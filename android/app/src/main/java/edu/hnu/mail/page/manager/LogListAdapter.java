package edu.hnu.mail.page.manager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import edu.hnu.mail.R;
import edu.hnu.mail.ui.mails.OnItemClickListener;

public class LogListAdapter extends RecyclerView.Adapter<LogListAdapter.ViewHolder> {

    private List<Log> logList;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private OnItemClickListener onItemClickListener;

    public void setLogList(List<Log> userList){
        this.logList = userList;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View root = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_log,parent,false);
        return new ViewHolder(root);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log log = logList.get(position);
        Date d = log.getCreated();
        if (d!=null){
            holder.created.setText(simpleDateFormat.format(d));
        }else{
            holder.created.setText("未知时间");
        }
        holder.log.setText(log.getLog());
    }

    @Override
    public int getItemCount() {
        return logList!=null?logList.size():0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView created;
        private TextView log;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            created = itemView.findViewById(R.id.created);
            log = itemView.findViewById(R.id.log);
        }
    }
}
