package com.example.learn_express;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MaterialAdapter extends RecyclerView.Adapter<MaterialAdapter.MaterialViewHolder> {
    private List<Material> materials;

    public MaterialAdapter(List<Material> materials) {
        this.materials = materials;
    }

    @NonNull
    @Override
    public MaterialViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_material, parent, false);
        return new MaterialViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MaterialViewHolder holder, int position) {
        Material material = materials.get(position);

        holder.tvCourseTag.setText(material.getCourseTitle() == null || material.getCourseTitle().trim().isEmpty() ? "General" : material.getCourseTitle());
        holder.tvTitle.setText(material.getTitle());
        holder.tvInstructor.setText("By " + (material.getInstructorName() == null || material.getInstructorName().trim().isEmpty() ? "Instructor" : material.getInstructorName()));

        String icon = "📚";
        if ("exam".equals(material.getType())) icon = "📝";
        else if ("quiz".equals(material.getType())) icon = "❓";
        else if ("video".equals(material.getType())) icon = "▶️";
        else if ("live_class".equals(material.getType())) icon = "🗣️";
        else if ("text".equals(material.getType())) icon = "📄";
        else if ("assignment".equals(material.getType())) icon = "📝";
        
        holder.tvIcon.setText(icon);

        if (material.getDueDate() != null && !material.getDueDate().isEmpty() && !material.getDueDate().equals("null")) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                Date date = sdf.parse(material.getDueDate());
                SimpleDateFormat outFormat = new SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.US);
                
                boolean isLate = new Date().after(date);
                if (isLate) {
                    holder.tvDate.setText("📅 " + outFormat.format(date) + " (LATE)");
                    holder.tvDate.setTextColor(Color.parseColor("#FF4D4D"));
                } else {
                    holder.tvDate.setText("📅 " + outFormat.format(date));
                    holder.tvDate.setTextColor(Color.parseColor("#64748B"));
                }
            } catch (Exception e) {
                holder.tvDate.setText("📅 " + material.getDueDate());
                holder.tvDate.setTextColor(Color.parseColor("#64748B"));
            }
        } else {
            holder.tvDate.setText("📅 No deadline");
            holder.tvDate.setTextColor(Color.parseColor("#64748B"));
        }

        if (material.getGradingCriteria() != null && !material.getGradingCriteria().isEmpty() && !material.getGradingCriteria().equals("null")) {
            holder.gradingContainer.setVisibility(View.VISIBLE);
            holder.tvGrading.setText(material.getGradingCriteria());
        } else {
            holder.gradingContainer.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return materials.size();
    }

    public static class MaterialViewHolder extends RecyclerView.ViewHolder {
        TextView tvCourseTag, tvDate, tvTitle, tvInstructor, tvGrading, tvIcon;
        LinearLayout gradingContainer;

        public MaterialViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCourseTag = itemView.findViewById(R.id.tvCourseTag);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvIcon = itemView.findViewById(R.id.tvIcon);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvInstructor = itemView.findViewById(R.id.tvInstructor);
            tvGrading = itemView.findViewById(R.id.tvGrading);
            gradingContainer = itemView.findViewById(R.id.gradingContainer);
        }
    }
}
