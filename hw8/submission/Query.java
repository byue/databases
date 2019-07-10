import java.io.FileInputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Random;

/**
 * Runs queries against a back-end database
 */
public class Query
{
	// how many times we reattempt when deadlock occurs
	private static final int DEADLOCKRETRIES = 10;
	private static final int DEADLOCKCODE = 1205;
	private static Random r = new Random();

	private String configFilename;
	private Properties configProps = new Properties();

	private String jSQLDriver;
	private String jSQLUrl;
	private String jSQLUser;
	private String jSQLPassword;

	// DB Connection
	private Connection conn;

	// Logged In User
	private String username; // customer username is unique

	// flight fields, itineraryID is the runnign index between the two lists
	private List<Flight> directFlights;
	private List<ArrayList<Flight>> indirectFlights;

	// Canned update queries
	private static final String CREATE_USER = "INSERT INTO USERS VALUES(?, ?, ?)";
	private PreparedStatement createUserStatement;

	private static final String CLEAR_TABLES = "TRUNCATE TABLE RESERVATIONS; DELETE FROM USERS; DELETE FROM BOOKINGS;";
	private PreparedStatement clearTableStatement;

	private static final String INCREMENT_BOOKINGS = "UPDATE BOOKINGS SET reservations = reservations + 1 WHERE fid = ? " + 
													 "IF @@ROWCOUNT = 0 " + 
													 "INSERT INTO BOOKINGS VALUES(?, 1)";
	private PreparedStatement incrementBookingsStatement;

	private static final String UPDATE_RESERVATIONS = "INSERT INTO RESERVATIONS VALUES(?, ?, ?, ?)";
	private PreparedStatement updateReservationsStatement;

	private static final String DECREMENT_BOOKINGS = "UPDATE BOOKINGS SET reservations = reservations - 1 WHERE fid = ? AND reservations > 0";
	private PreparedStatement decrementBookingsStatement;

	private static final String CANCEL_RESERVATION = "DELETE FROM RESERVATIONS WHERE username = ? AND rid = ?";
	private PreparedStatement cancelReservationStatement;

	private static final String ADD_BALANCE = "UPDATE USERS SET balance = balance + ? WHERE username = ?";
	private PreparedStatement addBalanceStatement;

	private static final String SET_PAID = "UPDATE RESERVATIONS SET paid = 1 WHERE rid = ?";
	private PreparedStatement setPaidStatement;

	// Canned select queries
	private static final String CHECK_FLIGHT_CAPACITY = "SELECT capacity FROM Flights WHERE fid = ?";
	private PreparedStatement checkFlightCapacityStatement;

	private static final String CHECK_HAS_USER = "SELECT TOP 1 * FROM Users WHERE username = ?";
	private PreparedStatement checkHasUserStatement;

	private static final String CHECK_HAS_RESERVATION = "SELECT TOP 1 * FROM RESERVATIONS AS r, FLIGHTS AS f WHERE username = ? AND r.fid1 = f.fid AND f.day_of_month = ?";
	private PreparedStatement checkHasReservationStatement;

	private static final String CHECK_FLIGHT_BOOKINGS = "SELECT reservations FROM BOOKINGS WHERE fid = ?";
	private PreparedStatement checkFlightBookingsStatement;

	private static final String CHECK_LOGIN = "SELECT TOP 1 * FROM Users WHERE username = ? AND password = ?";
	private PreparedStatement checkLoginStatement;

	private static final String GET_DIRECT_FLIGHTS = "SELECT TOP (?) f.fid, f.year, f.month_id, f.day_of_month, " +
													 "f.carrier_id, f.flight_num, f.origin_city, f.dest_city, f.actual_time, f.capacity, f.price " +
													 "FROM Flights AS f " +
													 "WHERE f.canceled = 0 AND f.origin_city = ? AND f.dest_city = ? AND f.day_of_month = ? " + 
													 "ORDER BY f.actual_time ASC, f.fid ASC";
	private PreparedStatement getDirectFlightsStatement;

private static final String GET_INDIRECT_FLIGHTS = "SELECT TOP (?) f1.fid, f1.year, f1.month_id, f1.day_of_month, " +
																  "f1.carrier_id, f1.flight_num, f1.origin_city, f1.dest_city, f1.actual_time, f1.capacity, f1.price, " +
																  "f2.fid, f2.year, f2.month_id, f2.day_of_month, " +
																  "f2.carrier_id, f2.flight_num, f2.origin_city, f2.dest_city, f2.actual_time, f2.capacity, f2.price " +
												   "FROM FLIGHTS AS f1 " +
												   "INNER JOIN FLIGHTS AS f2 ON (f1.dest_city = f2.origin_city AND f1.day_of_month = f2.day_of_month) " +
												   "WHERE f1.canceled = 0 AND f2.canceled = 0 AND f1.origin_city = ? AND f2.dest_city = ? AND f1.day_of_month = ? " + 
												   "ORDER BY f1.actual_time + f2.actual_time ASC, f1.fid ASC, f2.fid ASC";
	private PreparedStatement getIndirectFlightsStatement;

	// query will return RID, Paid, flight1 data, flight2 data (null if direct flight)
	private static final String GET_RESERVATIONS = "SELECT r.rid, r.paid, f1.fid, f1.year, f1.month_id, f1.day_of_month, " +
														  "f1.carrier_id, f1.flight_num, f1.origin_city, f1.dest_city, f1.actual_time, f1.capacity, f1.price, " +
														  "f2.fid, f2.year, f2.month_id, f2.day_of_month, " +
														  "f2.carrier_id, f2.flight_num, f2.origin_city, f2.dest_city, f2.actual_time, f2.capacity, f2.price " +
												   "FROM RESERVATIONS AS r " +
												   "INNER JOIN FLIGHTS AS f1 ON r.fid1 = f1.fid " +
												   "LEFT OUTER JOIN FLIGHTS AS f2 ON r.fid2 = f2.fid " +
												   "WHERE r.username = ? " + 
												   "ORDER BY r.rid ASC";
	private PreparedStatement getReservationsStatement;

	private static final String GET_RESERVATION_INFO = "SELECT r.fid1, r.fid2, r.paid, COALESCE(f1.price + f2.price, f1.price, f2.price) " + 
													   "FROM RESERVATIONS AS r " +
													   "INNER JOIN FLIGHTS AS f1 ON r.fid1 = f1.fid " +
													   "LEFT OUTER JOIN FLIGHTS AS f2 ON r.fid2 = f2.fid " +
													   "WHERE r.username = ? AND r.rid = ?";
	private PreparedStatement getReservationInfoStatement;

	private static final String GET_BALANCE = "SELECT u.balance FROM USERS AS u WHERE u.username = ?";
	private PreparedStatement getBalanceStatement;

	// transactions
	private static final String BEGIN_TRANSACTION_SQL = "SET TRANSACTION ISOLATION LEVEL SERIALIZABLE; BEGIN TRANSACTION;";
	private PreparedStatement beginTransactionStatement;

	private static final String COMMIT_SQL = "COMMIT TRANSACTION";
	private PreparedStatement commitTransactionStatement;

	private static final String ROLLBACK_SQL = "ROLLBACK TRANSACTION";
	private PreparedStatement rollbackTransactionStatement;

	class Flight
	{
		public int fid;
		public int year;
		public int monthId;
		public int dayOfMonth;
		public String carrierId;
		public String flightNum;
		public String originCity;
		public String destCity;
		public double time;
		public int capacity;
		public double price;

		public Flight(int fid, int year, int monthId, int dayOfMonth, String carrierId, 
					  String flightNum, String originCity, String destCity, double time, 
					  int capacity, double price) {
			this.fid = fid;
			this.year = year;
			this.monthId = monthId;
			this.dayOfMonth = dayOfMonth;
			this.carrierId = carrierId;
			this.flightNum = flightNum;
			this.originCity = originCity;
			this.destCity = destCity;
			this.time = time;
			this.capacity = capacity;
			this.price = price;
		}

		@Override
		public String toString()
		{
			return "ID: " + fid + " Date: " + year + "-" + monthId + "-" + dayOfMonth + " Carrier: " + carrierId +
				   " Number: " + flightNum + " Origin: " + originCity + " Dest: " + destCity + " Duration: " + time +
				   " Capacity: " + capacity + " Price: " + price;
		}
	}

	public Query(String configFilename)
	{
		this.configFilename = configFilename;
		this.directFlights = new ArrayList<Flight>();
		this.indirectFlights = new ArrayList<ArrayList<Flight>>();
	}

	/* Connection code to SQL Azure.  */
	public void openConnection() throws Exception
	{
		configProps.load(new FileInputStream(configFilename));

		jSQLDriver = configProps.getProperty("flightservice.jdbc_driver");
		jSQLUrl = configProps.getProperty("flightservice.url");
		jSQLUser = configProps.getProperty("flightservice.sqlazure_username");
		jSQLPassword = configProps.getProperty("flightservice.sqlazure_password");

		/* load jdbc drivers */
		Class.forName(jSQLDriver).newInstance();

		/* open connections to the flights database */
		conn = DriverManager.getConnection(jSQLUrl, // database
										   jSQLUser, // user
										   jSQLPassword); // password

		conn.setAutoCommit(true); //by default automatically commit after each statement

		/* You will also want to appropriately set the transaction's isolation level through:
			 conn.setTransactionIsolation(...)
			 See Connection class' JavaDoc for details.
		 */
		conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
	}

	public void closeConnection() throws Exception
	{
		conn.close();
	}

	/**
	 * Clear the data in any custom tables created. Do not drop any tables and do not
	 * clear the flights table. You should clear any tables you use to store reservations
	 * and reset the next reservation ID to be 1.
	 */
	public void clearTables()
	{
		// your code here
		while (true) {
			try {
				clearTableStatement.clearParameters();
				clearTableStatement.executeUpdate();
				return;
			} catch(SQLException e) {
				//e.printStackTrace();
			}
		}
	}

	/**
	 * prepare all the SQL statements in this method.
	 * "preparing" a statement is almost like compiling it.
	 * Note that the parameters (with ?) are still not filled in
	 */
	public void prepareStatements() throws Exception
	{
		// transaction markers
		beginTransactionStatement = conn.prepareStatement(BEGIN_TRANSACTION_SQL);
		commitTransactionStatement = conn.prepareStatement(COMMIT_SQL);
		rollbackTransactionStatement = conn.prepareStatement(ROLLBACK_SQL);
		// Canned Select Queries
		checkFlightCapacityStatement = conn.prepareStatement(CHECK_FLIGHT_CAPACITY);
		checkHasUserStatement = conn.prepareStatement(CHECK_HAS_USER);
		clearTableStatement = conn.prepareStatement(CLEAR_TABLES);
		checkLoginStatement = conn.prepareStatement(CHECK_LOGIN);
		getDirectFlightsStatement = conn.prepareStatement(GET_DIRECT_FLIGHTS);
		getIndirectFlightsStatement = conn.prepareStatement(GET_INDIRECT_FLIGHTS);
		checkHasReservationStatement = conn.prepareStatement(CHECK_HAS_RESERVATION);
		getReservationsStatement = conn.prepareStatement(GET_RESERVATIONS);
		getReservationInfoStatement = conn.prepareStatement(GET_RESERVATION_INFO);
		getBalanceStatement = conn.prepareStatement(GET_BALANCE);
		checkFlightBookingsStatement = conn.prepareStatement(CHECK_FLIGHT_BOOKINGS);
		// Canned Update Queries
		createUserStatement = conn.prepareStatement(CREATE_USER);
		incrementBookingsStatement = conn.prepareStatement(INCREMENT_BOOKINGS);
		updateReservationsStatement = conn.prepareStatement(UPDATE_RESERVATIONS, Statement.RETURN_GENERATED_KEYS);
		decrementBookingsStatement = conn.prepareStatement(DECREMENT_BOOKINGS);
		cancelReservationStatement = conn.prepareStatement(CANCEL_RESERVATION);
		addBalanceStatement = conn.prepareStatement(ADD_BALANCE);
		setPaidStatement = conn.prepareStatement(SET_PAID);
	}

	/**
	 * Takes a user's username and password and attempts to log the user in.
	 *
	 * @param username
	 * @param password
	 *
	 * @return If someone has already logged in, then return "User already logged in\n"
	 * For all other errors, return "Login failed\n".
	 *
	 * Otherwise, return "Logged in as [username]\n".
	 */
	public String transaction_login(String username, String password)
	{
		ResultSet hasLoginResult = null;
		int deadlockCount = 0;
		while (true) {
			try {
				if (this.username != null) {
					return "User already logged in\n";
				}
				checkLoginStatement.clearParameters();
				checkLoginStatement.setString(1, username);
				checkLoginStatement.setString(2, password);
				hasLoginResult = checkLoginStatement.executeQuery();
				// username and password not in database
				if (!hasLoginResult.next()) {
					return "Login failed\n";
				}
				this.username = username;
				resetFlights();
				return "Logged in as " + username + "\n";
			} catch(SQLException e) {
				if (deadlockCount == DEADLOCKRETRIES) {
					return "Login failed\n";
				}
				deadlockCount++;
				sleep(deadlockCount);
			} finally {
				closeResultSet(hasLoginResult);
			}
		}
	}

	/**
	 * Implement the create user function.
	 *
	 * @param username new user's username. User names are unique the system.
	 * @param password new user's password.
	 * @param initAmount initial amount to deposit into the user's account, should be >= 0 (failure otherwise).
	 *
	 * @return either "Created user {@code username}\n" or "Failed to create user\n" if failed.
	 */
	public String transaction_createCustomer(String username, String password, double initAmount)
	{
		if (initAmount < 0) {
			return "Failed to create user\n";
		}
		ResultSet hasUserResult = null;
		int deadlockCount = 0;
		boolean first = true;
		while (true) {
			begin();
			try {
				checkHasUserStatement.clearParameters();
				checkHasUserStatement.setString(1, username);
				hasUserResult = checkHasUserStatement.executeQuery();
				// user exists already
				if (hasUserResult.next()) {
					rollback();
					return "Failed to create user\n";
				}
				// user does not exist, create an entry
				createUserStatement.clearParameters();
				createUserStatement.setString(1, username);
				createUserStatement.setString(2, password);
				createUserStatement.setDouble(3, initAmount);
				createUserStatement.executeUpdate();
				commit();
				return "Created user " + username + "\n";
			} catch(SQLException e) {
				if (e.getErrorCode() != DEADLOCKCODE) {
					rollback();
				}
				if (deadlockCount ==  DEADLOCKRETRIES) {
					return "Failed to create user\n";
				}
				deadlockCount++;
				sleep(deadlockCount);
			} finally {
				closeResultSet(hasUserResult);
			}
		}
	}

	/**
	 * Implement the search function.
	 *
	 * Searches for flights from the given origin city to the given destination
	 * city, on the given day of the month. If {@code directFlight} is true, it only
	 * searches for direct flights, otherwise is searches for direct flights
	 * and flights with two "hops." Only searches for up to the number of
	 * itineraries given by {@code numberOfItineraries}.
	 *
	 * The results are sorted based on total flight time.
	 *
	 * @param originCity
	 * @param destinationCity
	 * @param directFlight if true, then only search for direct flights, otherwise include indirect flights as well
	 * @param dayOfMonth
	 * @param numberOfItineraries number of itineraries to return
	 *
	 * @return If no itineraries were found, return "No flights match your selection\n".
	 * If an error occurs, then return "Failed to search\n".
	 *
	 * Otherwise, the sorted itineraries printed in the following format:
	 *
	 * Itinerary [itinerary number]: [number of flights] flight(s), [total flight time] minutes\n
	 * [first flight in itinerary]\n
	 * ...
	 * [last flight in itinerary]\n
	 *
	 * Each flight should be printed using the same format as in the {@code Flight} class. Itinerary numbers
	 * in each search should always start from 0 and increase by 1.
	 *
	 * @see Flight#toString()
	 */
	public String transaction_search(String originCity, String destinationCity, boolean directFlight, int dayOfMonth, int numberOfItineraries)
	{
		int deadlockCount = 0;
		while (true) {
			resetFlights();
			begin();
			try {
				getDirectFlights(originCity, destinationCity, dayOfMonth, numberOfItineraries);
				int numDirectFlights = directFlights.size();
				if (directFlight && numDirectFlights == 0) {
					rollback();
					return "No flights match your selection\n";
				}
				if (!directFlight && numDirectFlights < numberOfItineraries) {
					getIndirectFlights(originCity, destinationCity, dayOfMonth, numberOfItineraries - numDirectFlights);
				}
				int numIndirectFlights = indirectFlights.size();
				if (numDirectFlights == 0 && numIndirectFlights == 0) {
					rollback();
					return "No flights match your selection\n";
				}
				commit();
				return getSearchResults().toString();
			} catch(SQLException e) {
				if (e.getErrorCode() != DEADLOCKCODE) {
					rollback();
				}
				if (deadlockCount == DEADLOCKRETRIES) {
					resetFlights();
					return "Failed to search\n";
				}
				deadlockCount++;
				sleep(deadlockCount);
			}
		}
	}

	/**
	 * Same as {@code transaction_search} except that it only performs single hop search and
	 * do it in an unsafe manner.
	 *
	 * @param originCity
	 * @param destinationCity
	 * @param directFlight
	 * @param dayOfMonth
	 * @param numberOfItineraries
	 *
	 * @return The search results. Note that this implementation *does not conform* to the format required by
	 * {@code transaction_search}.
	 */
	private String transaction_search_unsafe(String originCity, String destinationCity, boolean directFlight, int dayOfMonth, int numberOfItineraries)
	{
		StringBuffer sb = new StringBuffer();
		try
		{
			// one hop itineraries
			String unsafeSearchSQL = "SELECT TOP (" + numberOfItineraries + ") year,month_id,day_of_month,carrier_id,flight_num,origin_city,actual_time "
								   + "FROM Flights "
								   + "WHERE origin_city = \'" + originCity + "\' AND dest_city = \'" + destinationCity + "\' AND day_of_month =  " + dayOfMonth + " "
								   + "ORDER BY actual_time ASC";
			Statement searchStatement = conn.createStatement();
			ResultSet oneHopResults = searchStatement.executeQuery(unsafeSearchSQL);
			while (oneHopResults.next())
			{
				int result_year = oneHopResults.getInt("year");
				int result_monthId = oneHopResults.getInt("month_id");
				int result_dayOfMonth = oneHopResults.getInt("day_of_month");
				String result_carrierId = oneHopResults.getString("carrier_id");
				String result_flightNum = oneHopResults.getString("flight_num");
				String result_originCity = oneHopResults.getString("origin_city");
				int result_time = oneHopResults.getInt("actual_time");
				sb.append("Flight: " + result_year + "," + result_monthId + "," + result_dayOfMonth + "," + result_carrierId + "," + result_flightNum + "," + result_originCity + "," + result_time);
			}
			oneHopResults.close();
		} catch (SQLException e) { e.printStackTrace(); }
		return sb.toString();
	}

	/**
	 * Implements the book itinerary function.
	 *
	 * @param itineraryId ID of the itinerary to book. This must be one that is returned by search in the current session.
	 *
	 * @return If the user is not logged in, then return "Cannot book reservations, not logged in\n".
	 * If try to book an itinerary with invalid ID, then return "No such itinerary {@code itineraryId}\n".
	 * If the user already has a reservation on the same day as the one that they are trying to book now, then return
	 * "You cannot book two flights in the same day\n".
	 * For all other errors, return "Booking failed\n".
	 *
	 * And if booking succeeded, return "Booked flight(s), reservation ID: [reservationId]\n" where
	 * reservationId is a unique number in the reservation system that starts from 1 and increments by 1 each time a
	 * successful reservation is made by any user in the system.
	 */
	public String transaction_book(int itineraryId)
	{
		if (this.username == null) {
			return "Cannot book reservations, not logged in\n";
		}
		if (itineraryId < 0 || itineraryId >= directFlights.size() + indirectFlights.size()) {
			return "No such itinerary " + itineraryId + "\n";
		}
		int deadlockCount = 0;
		while (true) {
			// result sets
			ResultSet hasReservationResult = null;
			ResultSet flightFullResult1 = null;
			ResultSet flightFullResult2 = null;
			ResultSet reservationIdResult = null;
			begin();
			try {
				int directSize = directFlights.size();
				int indirectSize = indirectFlights.size();
				// find day of itinerary, fid of relevant flights (-1 means not set)
				int day = -1;
				int fid1 = -1;
				int fid2 = -1;
				if (itineraryId < directSize) {
					Flight f1 = directFlights.get(itineraryId);
					day = f1.dayOfMonth;
					fid1 = f1.fid;
				} else {
					ArrayList<Flight> tuple = indirectFlights.get(itineraryId - directSize);
					Flight f1 = tuple.get(0);
					Flight f2 = tuple.get(1);
					day = f1.dayOfMonth;
					fid1 = f1.fid;
					fid2 = f2.fid;
				}
				// check if existing reservations match same date
				checkHasReservationStatement.clearParameters();
				checkHasReservationStatement.setString(1, this.username);
				checkHasReservationStatement.setInt(2, day);
				hasReservationResult = checkHasReservationStatement.executeQuery();
				if (hasReservationResult.next()) {
					rollback();
					return "You cannot book two flights in the same day\n";
				}
				// check if flight is full or has 0 capacity
				int capacity1 = checkFlightCapacity(fid1);
				checkFlightBookingsStatement.clearParameters();
				checkFlightBookingsStatement.setInt(1, fid1);
				flightFullResult1 = checkFlightBookingsStatement.executeQuery();
				if (capacity1 == 0 || (flightFullResult1.next() && flightFullResult1.getInt(1) == capacity1)) {
					rollback();
					return "Booking failed\n";
				}
				// check if second flight is full or has 0 capacity
				if (fid2 != -1) {
					int capacity2 = checkFlightCapacity(fid2);
					checkFlightBookingsStatement.clearParameters();
					checkFlightBookingsStatement.setInt(1, fid2);
					flightFullResult2 = checkFlightBookingsStatement.executeQuery();
					if (capacity2 == 0 || (flightFullResult2.next() && flightFullResult2.getInt(1) == capacity2)) {
						rollback();
						return "Booking failed\n";
					}
				}
				// update bookings for first flight
				incrementBookingsStatement.clearParameters();
				setInt(incrementBookingsStatement, fid1);
				if (incrementBookingsStatement.executeUpdate() != 1) {
					rollback();
					return "Booking failed\n";
				}
				// update bookings for second flight
				if (fid2 != -1) {
					incrementBookingsStatement.clearParameters();
					setInt(incrementBookingsStatement, fid2);
					if (incrementBookingsStatement.executeUpdate() != 1) {
						rollback();
						return "Booking failed\n";
					}
				}
				// insert into reservations, OUTPUT reservation ID with getGeneratedKeys
				// We do not need to set the reservation ID as this is autogenerated
				updateReservationsStatement.clearParameters();
				updateReservationsStatement.setString(1, this.username);
				updateReservationsStatement.setInt(2, fid1);
				// set fid2 to -1 if this is a direct flight
				updateReservationsStatement.setInt(3, fid2);
				// set paid status to 0
				updateReservationsStatement.setInt(4, 0);
				if (updateReservationsStatement.executeUpdate() != 1) {
					rollback();
					return "Booking failed\n";
				}
				// get the reservation id
				reservationIdResult = updateReservationsStatement.getGeneratedKeys();
				if (reservationIdResult.next()) {
					int reservationId = reservationIdResult.getInt(1);
					commit();
					return "Booked flight(s), reservation ID: " + reservationId + "\n";
				}
				// error, could not get reservation Id
				rollback();
				return "Booking failed\n";
			} catch (SQLException e) {
				if (e.getErrorCode() != DEADLOCKCODE) {
					rollback();
				}
				if (deadlockCount == DEADLOCKRETRIES) {
					return "Booking failed\n";
				}
				deadlockCount++;
				sleep(deadlockCount);
			} finally {
				// cleanup result sets
				closeResultSet(hasReservationResult);
				closeResultSet(flightFullResult1);
				closeResultSet(flightFullResult2);
				closeResultSet(reservationIdResult);
			}
		}
	}

	/**
	 * Implements the reservations function.
	 *
	 * @return If no user has logged in, then return "Cannot view reservations, not logged in\n"
	 * If the user has no reservations, then return "No reservations found\n"
	 * For all other errors, return "Failed to retrieve reservations\n"
	 *
	 * Otherwise return the reservations in the following format:
	 *
	 * Reservation [reservation ID] paid: [true or false]:\n"
	 * [flight 1 under the reservation]
	 * [flight 2 under the reservation]
	 * Reservation [reservation ID] paid: [true or false]:\n"
	 * [flight 1 under the reservation]
	 * [flight 2 under the reservation]
	 * ...
	 *
	 * Each flight should be printed using the same format as in the {@code Flight} class.
	 *
	 * @see Flight#toString()
	 */
	public String transaction_reservations()
	{
		if (this.username == null) {
			return "Cannot view reservations, not logged in\n";
		}
		int deadlockCount = 0;
		while(true) {
			ResultSet reservationResult = null;
			StringBuffer sb = new StringBuffer();
			try {
				getReservationsStatement.clearParameters();
				getReservationsStatement.setString(1, this.username);
				reservationResult = getReservationsStatement.executeQuery();
				boolean hasReservation = false;
				while (reservationResult.next()) {
					hasReservation = true;
					int rid = reservationResult.getInt(1);
					int paid = reservationResult.getInt(2);
					sb.append("Reservation " + rid + " paid: ");
					if (paid == 1) {
						sb.append("true:\n");
					} else {
						sb.append("false:\n");
					}
					Flight f1 = new Flight(reservationResult.getInt(3), reservationResult.getInt(4),
										   reservationResult.getInt(5), reservationResult.getInt(6),
										   reservationResult.getString(7), reservationResult.getString(8),
										   reservationResult.getString(9), reservationResult.getString(10),
										   reservationResult.getDouble(11), reservationResult.getInt(12),
										   reservationResult.getDouble(13));
					sb.append(f1.toString() + "\n");
					// fid2 is 0 if the second flight was null in reservations
					int fid2 = reservationResult.getInt(14);
					if (fid2 != 0) {
						Flight f2 = new Flight(fid2, reservationResult.getInt(15),
											   reservationResult.getInt(16), reservationResult.getInt(17),
											   reservationResult.getString(18), reservationResult.getString(19),
											   reservationResult.getString(20), reservationResult.getString(21),
											   reservationResult.getDouble(22), reservationResult.getInt(23),
											   reservationResult.getDouble(24));
						sb.append(f2.toString() + "\n");
					}
				}
				if (!hasReservation) {
					return "No reservations found\n";
				}
				return sb.toString();
			} catch (SQLException e) {
				if (e.getErrorCode() != DEADLOCKCODE) {
					rollback();
				}
				if (deadlockCount == DEADLOCKRETRIES) {
					return "Failed to retrieve reservations\n";
				}
				deadlockCount++;
				sleep(deadlockCount);
			} finally {
				closeResultSet(reservationResult);
			}
		}
	}

	/**
	 * Implements the cancel operation.
	 *
	 * @param reservationId the reservation ID to cancel
	 *
	 * @return If no user has logged in, then return "Cannot cancel reservations, not logged in\n"
	 * For all other errors, return "Failed to cancel reservation [reservationId]"
	 *
	 * If successful, return "Canceled reservation [reservationId]"
	 *
	 * Even though a reservation has been canceled, its ID should not be reused by the system.
	 */
	public String transaction_cancel(int reservationId)
	{
		if (this.username == null) {
			return "Cannot cancel reservations, not logged in\n";
		}
		if (reservationId <= 0) {
			return "Failed to cancel reservation " + reservationId + "\n";
		}
		int deadlockCount = 0;
		while (true) {
			ResultSet flightResults = null;
			int fid1 = -1;
			int fid2 = -1;
			int paid = -1;
			double totalCost = 0;
			begin();
			try {
				// get reservation flight IDs
				getReservationInfoStatement.clearParameters();
				getReservationInfoStatement.setString(1, this.username);
				getReservationInfoStatement.setInt(2, reservationId);
				flightResults = getReservationInfoStatement.executeQuery();
				if (flightResults.next()) {
					fid1 = flightResults.getInt(1);
					fid2 = flightResults.getInt(2);
					paid = flightResults.getInt(3);
					totalCost = flightResults.getDouble(4);
				} else {
					rollback();
					return "Failed to cancel reservation " + reservationId + "\n";
				}
				// delete reservation from Reservation table
				cancelReservationStatement.clearParameters();
				cancelReservationStatement.setString(1, this.username);
				cancelReservationStatement.setInt(2, reservationId);
				// check if 1 row was deleted
				if (cancelReservationStatement.executeUpdate() != 1) {
					rollback();
					return "Failed to cancel reservation " + reservationId + "\n";
				}
				// decrement bookings for flights
				decrementBookingsStatement.clearParameters();
				decrementBookingsStatement.setInt(1, fid1);
				// check that flight was decremented
				if (decrementBookingsStatement.executeUpdate() != 1) {
					rollback();
					return "Failed to cancel reservation " + reservationId + "\n";
				}
				// decrement flight 2 if this was an indirect reservation
				if (fid2 != -1) {
					decrementBookingsStatement.clearParameters();
					decrementBookingsStatement.setInt(1, fid2);
					if (decrementBookingsStatement.executeUpdate() != 1) {
						rollback();
						return "Failed to cancel reservation " + reservationId + "\n";
					}
				}
				// update balance if user already paid for reservation
				if (paid == 1) {
					addBalanceStatement.clearParameters();
					addBalanceStatement.setDouble(1, totalCost);
					addBalanceStatement.setString(2, this.username);
					if (addBalanceStatement.executeUpdate() != 1) {
						rollback();
						return "Failed to cancel reservation " + reservationId + "\n";
					}
				}
				// successfully read flight IDs, deleted reservation, updated bookings, updated balance if necessary
				commit();
				return "Canceled reservation " + reservationId + "\n";
			} catch (SQLException e) {
				if (e.getErrorCode() != DEADLOCKCODE) {
					rollback();
				}
				if (deadlockCount == DEADLOCKRETRIES) {
					return "Failed to cancel reservation " + reservationId + "\n";
				}
				deadlockCount++;
				sleep(deadlockCount);
			} finally {
				closeResultSet(flightResults);
			}
		}
	}

	/**
	 * Implements the pay function.
	 *
	 * @param reservationId the reservation to pay for.
	 *
	 * @return If no user has logged in, then return "Cannot pay, not logged in\n"
	 * If the reservation is not found / not under the logged in user's name, then return
	 * "Cannot find unpaid reservation [reservationId] under user: [username]\n"
	 * If the user does not have enough money in their account, then return
	 * "User has only [balance] in account but itinerary costs [cost]\n"
	 * For all other errors, return "Failed to pay for reservation [reservationId]\n"
	 *
	 * If successful, return "Paid reservation: [reservationId] remaining balance: [balance]\n"
	 * where [balance] is the remaining balance in the user's account.
	 */
	public String transaction_pay (int reservationId)
	{
		if (this.username == null) {
			return "Cannot pay, not logged in\n";
		}
		if (reservationId <= 0) {
			return "Cannot find unpaid reservation " + reservationId + " under user: " + this.username + "\n";
		}
		int deadlockCount = 0;
		while (true) {
			ResultSet flightResults = null;
			ResultSet balanceResults = null;
			begin();
			try {
				// check if unpaid reservation exists and check cost
				getReservationInfoStatement.clearParameters();
				getReservationInfoStatement.setString(1, this.username);
				getReservationInfoStatement.setInt(2, reservationId);
				flightResults = getReservationInfoStatement.executeQuery();
				if (!flightResults.next() || flightResults.getInt(3) != 0) {
					rollback();
					return "Cannot find unpaid reservation " + reservationId + " under user: " + this.username + "\n"; 
				}
				double totalCost = flightResults.getDouble(4);
				// check if balance equal to or greater than cost
				getBalanceStatement.clearParameters();
				getBalanceStatement.setString(1, this.username);
				balanceResults = getBalanceStatement.executeQuery();
				if (!balanceResults.next()) {
					rollback();
					return "Failed to pay for reservation " + reservationId + "\n";
				}
				double balance = balanceResults.getDouble(1);
				if (Double.compare(balance, totalCost) < 0) {
					rollback();
					return "User has only " + balance + " in account but itinerary costs " + totalCost + "\n";
				}
				// update balance by subtracting cost
				addBalanceStatement.clearParameters();
				addBalanceStatement.setDouble(1, -totalCost);
				addBalanceStatement.setString(2, this.username);
				if (addBalanceStatement.executeUpdate() != 1) {
					rollback();
					return "Failed to pay for reservation " + reservationId + "\n";
				}
				// set reservation bit to paid (rid input)
				setPaidStatement.clearParameters();
				setPaidStatement.setInt(1, reservationId);
				if (setPaidStatement.executeUpdate() != 1) {
					rollback();
					return "Failed to pay for reservation " + reservationId + "\n";
				}
				commit();
				return "Paid reservation: " + reservationId + " remaining balance: " + (balance - totalCost) + "\n";
			} catch(SQLException e) {
				if (e.getErrorCode() != DEADLOCKCODE) {
					rollback();
				}
				if (deadlockCount == DEADLOCKRETRIES) {
					return "Failed to pay for reservation " + reservationId + "\n";
				}
				deadlockCount++;
				sleep(deadlockCount);
			} finally {
				closeResultSet(flightResults);
				closeResultSet(balanceResults);
			}
		}
	}

	/* some utility functions below */
	public void beginTransaction() throws SQLException
	{
		conn.setAutoCommit(false);
		beginTransactionStatement.executeUpdate();
	}

	public void commitTransaction() throws SQLException
	{
		commitTransactionStatement.executeUpdate();
		conn.setAutoCommit(true);
	}

	public void rollbackTransaction() throws SQLException
	{
		rollbackTransactionStatement.executeUpdate();
		conn.setAutoCommit(true);
	}

	private void begin()
	{
		try {
			beginTransaction();
		} catch (SQLException e) {
			//e.printStackTrace();
		}
	}

	private void commit()
	{
		try {
			commitTransaction();
		} catch (SQLException e) {
			//e.printStackTrace();
		}
	}

	private void rollback()
	{
		try {
			rollbackTransaction();
			return;
		} catch (SQLException e) {
			//e.printStackTrace();
		}
	}

	private void closeResultSet(ResultSet r) {
		try {
			if (r != null) {
				r.close();
			}
		} catch(SQLException e){
			// e.printStackTrace();
		}
	}

	private void resetFlights() {
		this.directFlights = new ArrayList<Flight>();
		this.indirectFlights = new ArrayList<ArrayList<Flight>>();
	}

	/**
	 * Shows an example of using PreparedStatements after setting arguments. You don't need to
	 * use this method if you don't want to.
	 */
	private int checkFlightCapacity(int fid) throws SQLException
	{
		checkFlightCapacityStatement.clearParameters();
		checkFlightCapacityStatement.setInt(1, fid);
		ResultSet results = checkFlightCapacityStatement.executeQuery();
		results.next();
		int capacity = results.getInt("capacity");
		results.close();

		return capacity;
	}

	private void getDirectFlights(String originCity, String destinationCity, int dayOfMonth, int numberOfItineraries) throws SQLException {
		ResultSet directResult = null;
		try {
			getDirectFlightsStatement.clearParameters();
			getDirectFlightsStatement.setInt(1, numberOfItineraries);
			getDirectFlightsStatement.setString(2, originCity);
			getDirectFlightsStatement.setString(3, destinationCity);
			getDirectFlightsStatement.setInt(4, dayOfMonth);
			directResult = getDirectFlightsStatement.executeQuery();
			while (directResult.next()) {
				this.directFlights.add(new Flight(directResult.getInt(1), directResult.getInt(2),
												  directResult.getInt(3), directResult.getInt(4),
												  directResult.getString(5), directResult.getString(6),
												  directResult.getString(7), directResult.getString(8),
												  directResult.getDouble(9), directResult.getInt(10),
												  directResult.getDouble(11)));
			}
		} catch (SQLException e) {
			throw new SQLException();
		} finally {
			closeResultSet(directResult);
		}
	}

	private void getIndirectFlights(String originCity, String destinationCity, int dayOfMonth, int numberOfItineraries) throws SQLException {
		ResultSet indirectResult = null;
		try {
			getIndirectFlightsStatement.clearParameters();
			getIndirectFlightsStatement.setInt(1, numberOfItineraries);
			getIndirectFlightsStatement.setString(2, originCity);
			getIndirectFlightsStatement.setString(3, destinationCity);
			getIndirectFlightsStatement.setInt(4, dayOfMonth);
			indirectResult = getIndirectFlightsStatement.executeQuery();
			while (indirectResult.next()) {
				ArrayList<Flight> tuple = new ArrayList<Flight>();
				tuple.add(new Flight(indirectResult.getInt(1), indirectResult.getInt(2),
									 indirectResult.getInt(3), indirectResult.getInt(4),
									 indirectResult.getString(5), indirectResult.getString(6),
									 indirectResult.getString(7), indirectResult.getString(8),
									 indirectResult.getDouble(9), indirectResult.getInt(10),
									 indirectResult.getDouble(11)));
				tuple.add(new Flight(indirectResult.getInt(12), indirectResult.getInt(13),
									 indirectResult.getInt(14), indirectResult.getInt(15),
									 indirectResult.getString(16), indirectResult.getString(17),
									 indirectResult.getString(18), indirectResult.getString(19),
									 indirectResult.getDouble(20), indirectResult.getInt(21),
									 indirectResult.getDouble(22)));
				this.indirectFlights.add(tuple);
			}
		} catch (SQLException e) {
			throw new SQLException();
		} finally {
			closeResultSet(indirectResult);
		}
	}

	private StringBuffer getSearchResults() {
		StringBuffer sb = new StringBuffer();
		int itineraryID = 0;
		for (Flight f : this.directFlights) {
			sb.append("Itinerary " + itineraryID + ": 1 flight(s), " + f.time + " minutes\n");
			sb.append(f.toString() + "\n");
			itineraryID++;
		}
		for (ArrayList<Flight> tuple : this.indirectFlights) {
			Flight f1 = tuple.get(0);
			Flight f2 = tuple.get(1);
			sb.append("Itinerary " + itineraryID + ": 2 flight(s), " + (f1.time + f2.time) + " minutes\n");
			sb.append(f1.toString() + "\n");
			sb.append(f2.toString() + "\n");
			itineraryID++;
		}
		return sb;
	}

	private void setInt(PreparedStatement p, int num) throws SQLException {
		for (int column = 1; column <= 2; column++) {
			p.setInt(column, num);
		}
	}

	// for pausing thread on deadlock recovery to prevent thrashing
	// utilizes exponential backoff/randomization of sleep times
	private void sleep(int deadlockCount) {
		try {
			Thread.sleep(deadlockCount * 200 + r.nextInt(500));
		} catch (InterruptedException e) {
			// e.printStackTrace();
		}
	}
}
