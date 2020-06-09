package edu.hnu.mail.ui.mails;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.chauthai.swipereveallayout.ViewBinderHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.hnu.mail.R;
import edu.hnu.mail.data.entity.Mail;
import edu.hnu.mail.page.mail.MailActivity;

public class MailListAdapter extends RecyclerView.Adapter<MailListAdapter.ViewHolder> {

    private List<Mail> mailList;




    private SimpleDateFormat monthDayFormat = new SimpleDateFormat("MM-dd");
    private SimpleDateFormat fullFormat = new SimpleDateFormat("yyyy-MM-dd");
    private int mailType;
    public static Long oneMinute = 60*1000L;
    public static Long oneHour = 60*oneMinute;
    public static Long oneDay = 24*oneHour;
    public static Long oneYear = 365*oneDay;
    private boolean showAvatar = false;
    private boolean isLoaded = false;

    private OnItemClickListener onItemClickListener;
    private OnSideClickListener onSideClickListener;
    private final ViewBinderHelper viewBinderHelper = new ViewBinderHelper();

    public MailListAdapter(int mailType){
        System.out.println("newMailListAdapter");
        this.mailType = mailType;
        viewBinderHelper.setOpenOnlyOne(true);
    }

    public interface OnSideClickListener{
        public void seenClick(int position,Mail mail);
        public void refuseClick(int position,Mail mail);
        public void deleteClick(int position,Mail mail);
    }

    public void setOnSideClickListener(OnSideClickListener onSideClickListener){
        this.onSideClickListener = onSideClickListener;
    }
    /**
     * 初始化邮件列表数据
     * @param list
     */
    public void setMailList(List<Mail> list){
        this.mailList = list;
    }

    /**
     * 载入新邮件到列表顶部
     * @param list
     */
    public void addMailList(List<Mail> list){
        if(mailList == null){
            mailList = new ArrayList<Mail>();
        }
        if(list!=null && list.size() > 0){
            this.mailList.addAll(0,list);
            notifyDataSetChanged();
        }
    }

    /**
     * 从列表中移除某个邮件
     * @param position
     */
    public void removeMail(int position){
        mailList.remove(position);
        notifyItemRemoved(position);
        if(position != mailList.size()){ // 如果移除的是最后一个，忽略
            notifyItemRangeChanged(position, mailList.size() - position);
        }
    }

    /**
     * 设置邮件星标
     * @param position
     * @param flag
     */
    public void setFlag(int position,int flag){
        mailList.get(position).setFlag(flag);
        notifyItemChanged(position);
    }

    /**
     * 设置为未读
     * @param position
     */
    public void setSeen(int position,int seen){
        mailList.get(position).setSeen(seen);
        System.out.println(seen);
        notifyItemChanged(position,"seen");
        viewBinderHelper.closeLayout( mailList.get(position).getUid());
    }

    public List<Mail> getMailList(){
        return  mailList;
    }

    public Mail getMail(int position){
        return mailList.get(position);
    }


    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mail,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder,int position,List payload) {
        if(payload.isEmpty()){
            onBindViewHolder(holder,position);
        }else{
            Mail mail = mailList.get(position);
            viewBinderHelper.bind(holder.swipeRevealLayout, mail.getUid());
            if (mail.getSeen()==0){
                holder.newMailIcon.setVisibility(View.VISIBLE);
                holder.seenText.setText("标为已读");
                holder.seenIcon.setImageResource(R.drawable.ic_read);
            }else{
                holder.newMailIcon.setVisibility(View.GONE);
                holder.seenText.setText("标为未读");
                holder.seenIcon.setImageResource(R.drawable.ic_unseen);
            }
        }
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder,int position) {
        Mail mail = mailList.get(position);
        viewBinderHelper.bind(holder.swipeRevealLayout, mail.getUid());
        holder.sendName.setText(mail.getFrom());
        holder.sendTime.setText(dateFormat(mail.getSendTime()));
        holder.subject.setText(mail.getSubject());
        holder.text_content.setText(mail.getTextContent());
        if(showAvatar){
            holder.avatar.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                    .load(mail.getAvatar())
                    .error(R.drawable.default_avatar)
                    .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                    .transition(DrawableTransitionOptions.withCrossFade(300))
                    .into(holder.avatar);
        }
        if (mail.getSeen()==0){
            holder.newMailIcon.setVisibility(View.VISIBLE);
            holder.seenText.setText("标为已读");
            holder.seenIcon.setImageResource(R.drawable.ic_read);
        }else{
            holder.newMailIcon.setVisibility(View.GONE);
            holder.seenText.setText("标为未读");
            holder.seenIcon.setImageResource(R.drawable.ic_unseen);
        }
        if(mail.getFlag()==1){
            holder.flagMailIcon.setVisibility(View.VISIBLE);
        }else{
            holder.flagMailIcon.setVisibility(View.GONE);
        }
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener!=null){
                    System.out.println("click mail: " + mailList.get(position));
                    onItemClickListener.onItemClick(position);
                }
            }
        });
        holder.buttonSeen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mail.getSeen() == 0){
                    mail.setSeen(1);
                }else{
                    mail.setSeen(0);
                }
                onSideClickListener.seenClick(position,mail);
            }
        });
        holder.buttonRefuse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSideClickListener.refuseClick(position,mail);
            }
        });
        holder.buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSideClickListener.deleteClick(position,mail);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mailList != null ? mailList.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private SwipeRevealLayout swipeRevealLayout;
        private LinearLayout buttonSeen;
        private TextView seenText;
        private LinearLayout buttonRefuse;
        private LinearLayout buttonDelete;
        private LinearLayout layout;
        private LinearLayout layoutAvatar;
        private ImageView avatar;
        private TextView sendName;
        private TextView subject;
        private TextView text_content;
        private TextView sendTime;
        private ImageView newMailIcon;
        private ImageView flagMailIcon;
        private ImageView seenIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            swipeRevealLayout = itemView.findViewById(R.id.swiper);
            buttonSeen = itemView.findViewById(R.id.button_seen);
            buttonRefuse = itemView.findViewById(R.id.button_refuses);
            seenText = itemView.findViewById(R.id.seen_text);
            buttonDelete = itemView.findViewById(R.id.button_delete);
            layout = itemView.findViewById(R.id.layout);
            avatar = itemView.findViewById(R.id.avatar);
            sendName = itemView.findViewById(R.id.send_name);
            subject = itemView.findViewById(R.id.send_subject);
            text_content = itemView.findViewById(R.id.text_content);
            sendTime = itemView.findViewById(R.id.send_time);
            newMailIcon = itemView.findViewById(R.id.new_mail);
            flagMailIcon = itemView.findViewById(R.id.flag_mail);
            seenIcon = itemView.findViewById(R.id.seen_icon);
        }
    }

    private String dateFormat(Date date){
        String format = null;
        if(date!=null){
            Long l = System.currentTimeMillis() - date.getTime();
            if(l < 3*oneMinute){
                format = "刚刚";
            }
            else if(l < oneHour){
                Long i = l / oneMinute;
                format = String.valueOf(i) + "分钟前";
            }else if(l < oneDay){
                Long i = l / oneHour;
                format = String.valueOf(i) + "小时前";
            }else if(l < oneYear){
                format = monthDayFormat.format(date);
            }else{
                format = fullFormat.format(date);
            }
        }else {
            format = "未知";
        }

        return format;
    }
}
