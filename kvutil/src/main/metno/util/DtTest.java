
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class DtTest {
	public static OffsetDateTime parse(String timestamp) {
		DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssx");
		System.out.println("In: '" + timestamp + "'");
		String ts = timestamp.replace('T', ' ');
		System.out.println("In [T]: '" + timestamp + "'  ->  '" + ts + "'");
		ts = ts.replace("z", "+00");
		System.out.println("In: [z]'" + timestamp + "'  ->  '" + ts + "'");
		ts = ts.replace("Z", "+00");
		System.out.println("In [Z]: '" + timestamp + "' -> '" + ts + "'");

		System.out.println("In [+]: " + ts.indexOf('+'));
		if (ts.lastIndexOf('+') < 0 && ts.lastIndexOf('-') <= 9)
			ts = ts + "+00";

		System.out.println("In: '" + timestamp + "'  ->  '" + ts + "'");
		System.out.println(ts);
		System.out.println("0123456789012345678901234567890");

		return OffsetDateTime.parse(ts, fmt);
	}

	public static void main(String[] args) {
		OffsetDateTime dt = DtTest.parse(args[0]);
		System.out.println("Instant: '" + dt.toInstant() + "'");
		System.out.println(args[0] + ": '" + dt.format(DateTimeFormatter.ISO_INSTANT) + "'");
	}

}