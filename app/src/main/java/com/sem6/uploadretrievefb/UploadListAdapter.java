package com.sem6.uploadretrievefb;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UploadListAdapter extends RecyclerView.Adapter<UploadListAdapter.ViewHolder> {

    public List<String> fileNameList;
    public List<String> fileDoneList;

    public UploadListAdapter(List<String> fileNameList,List<String> fileDoneList){
        this.fileDoneList = fileDoneList;
        this.fileNameList = fileNameList;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_single,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String fileName = fileNameList.get(position);
        holder.fileNameView.setText(fileName);

        String fileDone = fileDoneList.get(position);

        if(fileDone.equals("uploading")){
            holder.fileDoneView.setImageResource(R.drawable.outline_add_black_48);
        }
        else{
            holder.fileDoneView.setImageResource(R.drawable.outline_check_circle_outline_black_48);
        }

    }

    @Override
    public int getItemCount() {
        return fileNameList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public TextView fileNameView;
        public ImageView fileDoneView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;

            fileNameView = mView.findViewById(R.id.upload_fileName);
            fileDoneView = mView.findViewById(R.id.upload_loading);

        }
    }
}
