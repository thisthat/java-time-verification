public class Metrics {


	private Test(int i, int k) {
		int month = 8;
		String monthString;
		switch (month) {
			case 0:
			case 1:
				monthString = "January";
				break;
			case 2:
				monthString = "February";
				break;
				//10
			case 3:
				monthString = "March";
				break;
			case 4:
				monthString = "April";
				break;
			case 5:
				monthString = "May";
				break;
			case 6:
			case 7:
			case 8:
				monthString = "June";
				break;
			case 10:
				monthString = "October";
				break;
			case 11:
				monthString = "November";
				break;
			case 12:
				monthString = "December";
				break;
			default:
				monthString = "Invalid month";
				break;
		}
		System.out.println(monthString);
//37
		synchronized (monthString) {
			//inside the
			System.out.print(stmttt.toString());
			int i, j;
		}
//40
		System.out.println(monthString);

		try {
			bw.write("");
		} catch (Exception e) {
			throw new Exception("Exc");
			System.out.print("str");
		} catch (IOException e) {
			throw new Exception("IO Exc");
			System.out.print("str");
		} finally {
			System.err.print("Eccezione lanciata");
			System.out.print("str");
			System.err.print("Eccezione lanciata");
			System.out.print("str");
		}
//54
	}
}