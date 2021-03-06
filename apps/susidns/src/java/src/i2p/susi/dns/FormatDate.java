package i2p.susi.dns;

import java.util.Date;
import java.text.DateFormat;

import net.i2p.I2PAppContext;
import net.i2p.data.DataHelper;

/**
 * Format a date in local time zone
 * @since 0.8.7
 */
public abstract class FormatDate
{
    private static final DateFormat _dateFormat;

    static {
	DateFormat fmt = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
	// the router sets the JVM time zone to UTC but saves the original here so we can get it
        fmt.setTimeZone(DataHelper.getSystemTimeZone(I2PAppContext.getGlobalContext()));
	_dateFormat = fmt;
    }

    public static String format(long date)
    {
	synchronized(_dateFormat) {
		return _dateFormat.format(new Date(date));
	}
    }
}
