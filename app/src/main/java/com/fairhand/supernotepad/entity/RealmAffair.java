package com.fairhand.supernotepad.entity;

import io.realm.RealmObject;
import io.realm.annotations.Required;

/**
 * 单独事件实体类
 * 事件，，，我是服了
 *
 * @author Phanton
 * @date 11/24/2018 - Saturday - 7:33 PM
 */
public class RealmAffair extends RealmObject {
    
    private String key;
    
    @Required
    private String title;
    @Required
    private String time;
    private String backup;
    private boolean remind;
    
    private int kindIcon;
    private String kindName;
    
    public int getKindIcon() {
        return kindIcon;
    }
    
    public void setKindIcon(int kindIcon) {
        this.kindIcon = kindIcon;
    }
    
    public String getKindName() {
        return kindName;
    }
    
    public void setKindName(String kindName) {
        this.kindName = kindName;
    }
    
    public String getKey() {
        return key;
    }
    
    public void setKey(String key) {
        this.key = key;
    }
    
    public boolean isRemind() {
        return remind;
    }
    
    public void setRemind(boolean remind) {
        this.remind = remind;
    }
    
    public String getBackup() {
        return backup;
    }
    
    public void setBackup(String backup) {
        this.backup = backup;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getTime() {
        return time;
    }
    
    public void setTime(String time) {
        this.time = time;
    }
    
}
