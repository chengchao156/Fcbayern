package cn.fcbayern.android.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by chenzhan on 15/6/8.
 */
public class DateUtils {

    public static String getDate(long time) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINESE);
        Date d = new Date(time * 1000);
        return format.format(d);
    }

    public static String getTime(long time) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.CHINESE);
        Date d = new Date(time * 1000);
        return format.format(d);
    }

    private static final Calendar CALENDAR = Calendar.getInstance();
    private static final SimpleDateFormat SHOW_FORMAT_TIME = new SimpleDateFormat("HH:mm", Locale.CHINESE);
    private static final SimpleDateFormat SHOW_FORMAT_DATE = new SimpleDateFormat("MM-dd");
    private static final SimpleDateFormat SERVER_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static final SimpleDateFormat SHOW_FORMAT_BIRTHDAY = new SimpleDateFormat("yyyy.MM.dd", Locale.CHINESE);
    private static final SimpleDateFormat[] SERVER_FORMAT_BIRTHDAY = {new SimpleDateFormat("d MMMM,y", Locale.US), new SimpleDateFormat("d MMMM y", Locale.US), new SimpleDateFormat("y年d月M日")};

    public static String getShowTime(String timeStr) {
        try {
            Date date = SERVER_FORMAT.parse(timeStr);
            CALENDAR.setTimeInMillis(System.currentTimeMillis());
            CALENDAR.set(CALENDAR.get(Calendar.YEAR), CALENDAR.get(Calendar.MONTH), CALENDAR.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
            Date t = CALENDAR.getTime();
            if (date.after(t)) {
                return SHOW_FORMAT_TIME.format(date);
            }
            return SHOW_FORMAT_DATE.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * *将时间戳转为代表"距现在多久之前"的字符串
     *
     * @param timeStr
     * @return
     */
    public static String getStandardDate(String timeStr) {

        StringBuffer sb = new StringBuffer();

        long t = Long.parseLong(timeStr);
        long time = System.currentTimeMillis() - (t * 1000);
        long mill = (long) Math.ceil(time / 1000);//秒前

        long minute = (long) Math.ceil(time / 60 / 1000.0f);// 分钟前

        long hour = (long) Math.ceil(time / 60 / 60 / 1000.0f);// 小时

        long day = (long) Math.ceil(time / 24 / 60 / 60 / 1000.0f);// 天前

        if (day - 1 > 0 && day - 1 <= 30) {
            sb.append(day + "天");
        } else if (day > 30) {
            return sb.append(getDate(t)).toString();
        } else if (hour - 1 > 0) {
            if (hour >= 24) {
                sb.append("1天");
            } else {
                sb.append(hour + "小时");
            }
        } else if (minute - 1 > 0) {
            if (minute == 60) {
                sb.append("1小时");
            } else {
                sb.append(minute + "分钟");
            }
        } else if (mill - 1 > 0) {
            if (mill == 60) {
                sb.append("1分钟");
            } else {
                sb.append(mill + "秒");
            }
        } else {
            sb.append("刚刚");
        }
        if (!sb.toString().equals("刚刚")) {
            sb.append("前");
        }
        return sb.toString();
    }

//    public static String getBirthday(String dateStr) {
//        try {
//            Date date = SERVER_FORMAT_BIRTHDAY1.parse(dateStr);
//            return SHOW_FORMAT_BIRTHDAY.format(date);
//        } catch (ParseException e) {
//            try {
//                Date date = SERVER_FORMAT_BIRTHDAY2.parse(dateStr);
//                return SHOW_FORMAT_BIRTHDAY.format(date);
//            } catch (ParseException e1) { }
//        }
//        return dateStr;
//    }
}
