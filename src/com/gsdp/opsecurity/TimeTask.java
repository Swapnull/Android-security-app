package com.gsdp.opsecurity;

import android.content.ContextWrapper;

import com.buzzbox.mob.android.scheduler.NotificationMessage;
import com.buzzbox.mob.android.scheduler.Task;
import com.buzzbox.mob.android.scheduler.TaskResult;

public class TimeTask implements Task {

	@Override
	public TaskResult doWork(ContextWrapper ctx) {
        TaskResult res = new TaskResult();
        
        // TODO implement your business logic here
        // i.e. query the DB, connect to a web service using HttpUtils, etc..
        
        NotificationMessage notification = new NotificationMessage(
                "Hello World",
                "Don't forget to open Hello World App");
        notification.setNotificationClickIntentClass(UserSettings.class);
        res.addMessage( notification );   
        
        return res;
	}

	@Override
	public String getId() {
		return "pref_picRate";
	}

	@Override
	public String getTitle() {
		// TODO Auto-generated method stub
		return "Picture Rate";
	}
	
}
