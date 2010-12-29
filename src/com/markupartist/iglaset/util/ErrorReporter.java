package com.markupartist.iglaset.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Date;
import java.util.Random;

import com.markupartist.iglaset.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.os.StatFs;

/**
 * Error reporter.
 * http://androidblogger.blogspot.com/2009/12/how-to-improve-your-application-crash.html
 */
public class ErrorReporter implements Thread.UncaughtExceptionHandler {
    String mVersionName;
    String mPackageName;
    String mFilePath;
    String mPhoneModel;
    String mAndroidVersion;
    String mBoard;
    String mBrand;
    // String CPU_ABI;
    String mDevice;
    String mDisplay;
    String mFingerPrint;
    String mHost;
    String mId;
    String mManufacturer;
    String mModel;
    String mProduct;
    String mTags;
    long mTime;
    String mType;
    String mUser;

    private Thread.UncaughtExceptionHandler mPreviousHandler;
    private Context mCurContext;

    public void init(Context context) {
        mPreviousHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
        recoltInformations(context);
        mCurContext = context;
    }

    public long getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return availableBlocks * blockSize;
    }

    public long getTotalInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        return totalBlocks * blockSize;
    }

    void recoltInformations(Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo pi;
            // Version
            pi = pm.getPackageInfo(context.getPackageName(), 0);
            mVersionName = pi.versionName;
            // Package name
            mPackageName = pi.packageName;
            // Files dir for storing the stack traces
            mFilePath = context.getFilesDir().getAbsolutePath();
            // Device model
            mPhoneModel = android.os.Build.MODEL;
            // Android version
            mAndroidVersion = android.os.Build.VERSION.RELEASE;

            mBoard = android.os.Build.BOARD;
            mBrand = android.os.Build.BRAND;
            // CPU_ABI = android.os.Build.;
            mDevice = android.os.Build.DEVICE;
            mDisplay = android.os.Build.DISPLAY;
            mFingerPrint = android.os.Build.FINGERPRINT;
            mHost = android.os.Build.HOST;
            mId = android.os.Build.ID;
            mModel = android.os.Build.MODEL;
            mManufacturer = android.os.Build.MANUFACTURER;
            mProduct = android.os.Build.PRODUCT;
            mTags = android.os.Build.TAGS;
            mTime = android.os.Build.TIME;
            mType = android.os.Build.TYPE;
            mUser = android.os.Build.USER;

        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void appendLog(StringBuilder builder, String key, String value) {
    	builder.append(key).append(" : ").append(value).append("\n");
    }
    
    public String createInformationString() {
    	
    	StringBuilder builder = new StringBuilder();
    	appendLog(builder, "Version", mVersionName);
    	appendLog(builder, "Package", mPackageName);
    	appendLog(builder, "FilePath", mFilePath);
    	appendLog(builder, "Phone Model", mPhoneModel);
    	appendLog(builder, "Android Version", mAndroidVersion);
    	appendLog(builder, "Board", mBoard);
    	appendLog(builder, "Brand", mBrand);
    	appendLog(builder, "Device", mDevice);
    	appendLog(builder, "Display", mDisplay);
    	appendLog(builder, "Finger Print", mFingerPrint);
    	appendLog(builder, "Host", mHost);
    	appendLog(builder, "ID", mId);
    	appendLog(builder, "Model", mModel);
    	appendLog(builder, "Manufacturer", mManufacturer);
    	appendLog(builder, "Product", mProduct);
    	appendLog(builder, "Tags", mTags);
    	appendLog(builder, "Time", Long.toString(mTime));
    	appendLog(builder, "Type", mType);
    	appendLog(builder, "User", mUser);
    	appendLog(builder, "Total Internal Memory", Long.toString(getTotalInternalMemorySize()));
    	appendLog(builder, "Available Internal Memory", Long.toString(getAvailableInternalMemorySize()));

        return builder.toString();
    }

    public void uncaughtException(Thread t, Throwable e) {
        Date CurDate = new Date();
        StringBuffer report = new StringBuffer();
        report.append("Error Report collected on : ").append(CurDate.toString());
        report.append("\n");
        report.append("\n");
        report.append("Informations :");
        report.append("\n");
        report.append("==============");
        report.append("\n");
        report.append("\n");
        report.append(createInformationString());

        report.append("\n\n");
        report.append("Stack : \n");
        report.append("======= \n");
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        e.printStackTrace(printWriter);
        String stacktrace = result.toString();
        report.append(stacktrace);

        report.append("\n");
        report.append("Cause : \n");
        report.append("======= \n");

        // If the exception was thrown in a background thread inside
        // AsyncTask, then the actual exception can be found with getCause
        Throwable cause = e.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            report.append(result.toString());
            cause = cause.getCause();
        }
        printWriter.close();
        report.append("****  End of current Report ***");
        saveAsFile(report.toString());
        // SendErrorMail( Report );
        mPreviousHandler.uncaughtException(t, e);
    }

    private static class SingletonHolder {
    	public static final ErrorReporter instance = new ErrorReporter();
    }
    
    public static ErrorReporter getInstance() {
    	return SingletonHolder.instance;
    }

    private void sendErrorMail(Context context, String ErrorContent) {
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        String subject = context.getResources().getString(
                R.string.crash_report_mail_subject);
        String body = context.getResources().getString(
                R.string.crash_report_mail_body)
                + "\n\n" + ErrorContent + "\n\n";
        sendIntent.putExtra(Intent.EXTRA_EMAIL,
                new String[] { context.getResources().getString(
                        R.string.crash_report_email) });
        sendIntent.putExtra(Intent.EXTRA_TEXT, body);
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        sendIntent.setType("message/rfc822");
        context.startActivity(Intent.createChooser(sendIntent, "Title:"));
    }

    private void saveAsFile(String ErrorContent) {
        try {
            Random generator = new Random();
            int random = generator.nextInt(99999);
            String FileName = "stack-" + random + ".stacktrace";
            FileOutputStream trace = mCurContext.openFileOutput(FileName,
                    Context.MODE_PRIVATE);
            trace.write(ErrorContent.getBytes());
            trace.close();
        } catch (IOException ioe) {
            // ...
        }
    }

    private String[] getErrorFileList() {
        File dir = new File(mFilePath + "/");
        // Try to create the files folder if it doesn't exist
        dir.mkdir();
        // Filter for ".stacktrace" files
        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".stacktrace");
            }
        };
        return dir.list(filter);
    }

    private boolean isThereAnyErrorFile() {
        return getErrorFileList().length > 0;
    }

    public void checkErrorAndSendMail(Context context) {
        try {
            if (isThereAnyErrorFile()) {
            	StringBuffer buffer = new StringBuffer(); 
                String[] ErrorFileList = getErrorFileList();
                int curIndex = 0;
                // We limit the number of crash reports to send ( in order not
                // to be too slow )
                final int MaxSendMail = 5;
                for (String curString : ErrorFileList) {
                    if (curIndex++ <= MaxSendMail) {
                        buffer.append("New Trace collected :\n");
                        buffer.append("=====================\n ");
                        String filePath = mFilePath + "/" + curString;
                        BufferedReader input = new BufferedReader(
                                new FileReader(filePath));
                        String line;
                        while ((line = input.readLine()) != null) {
                            buffer.append(line).append("\n");
                        }
                        input.close();
                    }

                    // DELETE FILES !!!!
                    //File curFile = new File(FilePath + "/" + curString);
                    //curFile.delete();
                    deleteFile(curString);
                }
                sendErrorMail(context, buffer.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteFile(String file) {
        // DELETE FILES !!!!
        File curFile = new File(mFilePath + "/" + file);
        curFile.delete();
    }

    public void checkErrorAndReport(final Context context) {
        try {
            if (isThereAnyErrorFile()) {
                new AlertDialog.Builder(context)
                        .setTitle(context.getText(R.string.crash_report_dialog_title))
                        .setMessage(context.getText(R.string.crash_report_dialog_message))
                        .setPositiveButton(context.getText(R.string.yes), new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                checkErrorAndSendMail(context);
                            }
                        }).setNegativeButton(context.getText(R.string.cancel), new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                try {
                                    if (isThereAnyErrorFile()) {
                                        String[] ErrorFileList = getErrorFileList();
                                        for (String curString : ErrorFileList) {
                                            deleteFile(curString);
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        })
                        .show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
