package edu.hnu.mail.ui.mails;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import edu.hnu.mail.R;
import edu.hnu.mail.data.entity.Attachment;

public class FileListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Attachment> attachmentList = new ArrayList<Attachment>();
    private OnItemClickListener onItemClickListener;
    private OnItemClickListener onItemClickListener2;


    public List<Attachment> getAttachmentList(){
        return attachmentList;
    }

    public int getLength(){
        return attachmentList.size();
    }

    public Attachment getAttachment(int position){
        return attachmentList.get(position);
    }

    public void setAttachmentList(List<Attachment> attachmentList){
        this.attachmentList = attachmentList;
        notifyDataSetChanged();
    }

    public void addAttachment(Attachment attachment){
        attachmentList.add(attachment);
        notifyItemInserted(attachmentList.size()-1);
    }

    public void deleteAttachment(int position){
        attachmentList.remove(position);
        notifyItemRemoved(position);
        if(position != attachmentList.size()){ // 如果移除的是最后一个，忽略
            notifyItemRangeChanged(position, attachmentList.size() - position);
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnItemClickListener2(OnItemClickListener onItemClickListener){
        this.onItemClickListener2 = onItemClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 0) {
            View root = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_file, parent, false);
            return new ViewHolder(root);
        }else{
            View root = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_file_add, parent, false);
            return new ViewHolder2(root);
        }
    }

    @Override
    public int getItemViewType(int position){
        if(position!=getItemCount()-1){
            return 0;
        }else{
            return 1;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(getItemViewType(position)==0) {
            ViewHolder holder2 = (ViewHolder) holder;
            Attachment attachment = attachmentList.get(position);
            holder2.name.setText(attachment.getName());
            System.out.println("fileType:" + attachment.getFileType());
            if(attachment.getFileType().startsWith("image")){
                if(attachment.getUri()!=null){
                    Glide.with(holder.itemView.getContext())
                            .load(attachment.getUri())
                            .error(R.drawable.ic_file_cover)
                            .into(holder2.cover);
                }else if(attachment.getPath()!=null){
                    File file = new File(attachment.getPath());
                    if (!file.exists()){
                        file = new File(holder.itemView.getContext().getFilesDir(),attachment.getPath());
                    }
                    Glide.with(holder.itemView.getContext())
                            .load(file)
                            .error(R.drawable.ic_file_cover)
                            .into(holder2.cover);
                }
            }else {
                Glide.with(holder.itemView.getContext())
                        .load(R.drawable.ic_file_cover)
                        .into(holder2.cover);
            }
            holder2.layout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    onItemClickListener.onItemClick(position);
                    return false;
                }
            });
        }else {
            ViewHolder2 holder2 = (ViewHolder2) holder;
            holder2.layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener2.onItemClick(position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return attachmentList!=null?attachmentList.size()+1:1;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout layout;
        private ImageView cover;
        private TextView name;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.layout);
            cover = itemView.findViewById(R.id.file_cover);
            name = itemView.findViewById(R.id.filename);
        }
    }

    public class ViewHolder2 extends RecyclerView.ViewHolder {

        private LinearLayout layout;

        public ViewHolder2(@NonNull View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.layout);
        }
    }
}
