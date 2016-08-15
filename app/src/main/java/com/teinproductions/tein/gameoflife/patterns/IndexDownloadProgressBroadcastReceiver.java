package com.teinproductions.tein.gameoflife.patterns;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.teinproductions.tein.gameoflife.R;


public class IndexDownloadProgressBroadcastReceiver extends BroadcastReceiver {
    private static final int NOTIFICATION_ID = 1;

    public IndexDownloadProgressBroadcastReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int maxProgress = intent.getIntExtra(NamesDownloadService.PROGRESS_BAR_MAX, 685);
        int progress = intent.getIntExtra(NamesDownloadService.PROGRESS_BAR_PROGRESS, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher);

        if (progress == maxProgress - 1) {
            builder.setContentTitle("Game of Life: downloading done");
        } else {
            builder.setContentTitle("Downloading pattern names...")
                    .setProgress(maxProgress, progress, false)
                    .setContentText(progress + " / " + maxProgress);
        }

        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, builder.build());
    }
}
