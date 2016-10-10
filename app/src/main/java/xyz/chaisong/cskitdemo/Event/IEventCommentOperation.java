package xyz.chaisong.cskitdemo.Event;

/**
 * Created by song on 16/10/10.
 */

public interface IEventCommentOperation {
    void comment(String commentType,String commentUserName,int articleOrPaperId,int CommentId);
}