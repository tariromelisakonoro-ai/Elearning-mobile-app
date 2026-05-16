package com.example.learn_express;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.ViewHolder> {
    private List<Course> courses;

    public CourseAdapter(List<Course> courses) {
        this.courses = courses;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_course, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Course course = courses.get(position);
        holder.courseTitle.setText(course.getTitle());
        holder.courseDesc.setText(course.getDescription());
        holder.courseInstructor.setText("by " + course.getInstructorName());
    }

    @Override
    public int getItemCount() {
        return courses.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView courseTitle;
        public TextView courseDesc;
        public TextView courseInstructor;

        public ViewHolder(View itemView) {
            super(itemView);
            courseTitle = itemView.findViewById(R.id.courseTitle);
            courseDesc = itemView.findViewById(R.id.courseDesc);
            courseInstructor = itemView.findViewById(R.id.courseInstructor);
        }
    }
}