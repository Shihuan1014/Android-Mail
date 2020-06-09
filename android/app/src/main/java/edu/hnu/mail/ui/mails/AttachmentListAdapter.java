package edu.hnu.mail.ui.mails;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.List;

import edu.hnu.mail.R;
import edu.hnu.mail.data.entity.Attachment;
import edu.hnu.mail.util.FileUtil;

public class AttachmentListAdapter extends RecyclerView.Adapter<AttachmentListAdapter.ViewHolder> {

    private List<Attachment> attachmentList;
    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }

    public String getAttachmentPath(int position){
        return attachmentList.get(position).getPath();
    }


    public void setAttachmentList(List<Attachment> attachmentList){
        this.attachmentList = attachmentList;
        notifyDataSetChanged();
    }



    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mail_attachment,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Attachment attachment = attachmentList.get(position);
        holder.name.setText(attachment.getName());
        System.out.println(attachment);
        if(attachment.getFileType()!=null && attachment.getFileType().startsWith("image")){
            if(attachment.getPath()!=null){
                File createFiles = new File(holder.itemView.getContext().getFilesDir(),
                        attachment.getPath());
                if(!createFiles.exists()){
                    createFiles = new File(attachment.getPath());
                }
                Glide.with(holder.itemView.getContext())
                        .load(createFiles)
                        .error(R.drawable.ic_file_cover)
                        .into(holder.fileCover);
                holder.layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(onItemClickListener!=null){
                            onItemClickListener.onItemClick(position);
                        }
                    }
                });
            }else{
                Glide.with(holder.itemView.getContext())
                        .load(R.drawable.ic_file_cover)
                        .into(holder.fileCover);
            }
        }else {
            Glide.with(holder.itemView.getContext())
                    .load(R.drawable.ic_file_cover)
                    .into(holder.fileCover);
        }
        holder.size.setText(String.valueOf(formatFileSize(attachment.getSize())));
    }

    @Override
    public int getItemCount() {
        return attachmentList!=null?attachmentList.size():0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private LinearLayout layout;
        private ImageView fileCover;
        private TextView name;
        private TextView size;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.layout);
            fileCover = itemView.findViewById(R.id.file_cover);
            name = itemView.findViewById(R.id.name);
            size = itemView.findViewById(R.id.size);
        }
    }

    /**
     * 转换文件大小成KB  M等
     */
    private String formatFileSize(long fileS) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "K";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "M";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "G";
        }
        return fileSizeString;
    }
}
