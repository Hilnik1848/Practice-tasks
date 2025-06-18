package com.example.travenor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.travenor.Models.Booking;
import com.example.travenor.Models.Hotel;
import com.example.travenor.Models.HotelRoom;
import java.util.List;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder> {

    private List<Booking> bookings;
    private Context context;

    public ScheduleAdapter(List<Booking> bookings, Context context) {
        this.bookings = bookings;
        this.context = context;
    }

    @NonNull
    @Override
    public ScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.schedule_item, parent, false);
        return new ScheduleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleViewHolder holder, int position) {
        Booking booking = bookings.get(position);
        Hotel hotel = booking.getHotel();
        HotelRoom hotelRoom = booking.getHotelRoom();

        if (hotel != null) {
            String imageUrl = hotel.getImageUrl();
            if (imageUrl != null && !imageUrl.startsWith("http")) {
                imageUrl = "https://mmbdesfnabtcbpjwcwde.supabase.co/storage/v1/object/public/hotel/" + imageUrl;
            }

            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.img1)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(holder.imageView);

            holder.destinationName.setText(hotel.getName());
            holder.location.setText(hotel.getAddres());

            String dateText = booking.getCheckInDate() + " - " + booking.getCheckOutDate();
            holder.date.setText(dateText);

            if (hotelRoom != null && hotelRoom.getRoom_type() != null) {
                holder.roomType.setText(hotelRoom.getRoom_type());
            } else {
                holder.roomType.setText("Тип номера не указан");
            }

            if (booking.getSumm() != null) {
                holder.price.setText(String.format("%,d ₽", booking.getSumm().intValue()));
            } else {
                holder.price.setText("Цена не указана");
            }
        }
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    static class ScheduleViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView destinationName, location, date, price, roomType;

        public ScheduleViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.scheduleItemImage);
            destinationName = itemView.findViewById(R.id.scheduleItemDestinationName);
            location = itemView.findViewById(R.id.scheduleItemLocation);
            date = itemView.findViewById(R.id.scheduleItemDate);
            price = itemView.findViewById(R.id.scheduleItemPrice);
            roomType = itemView.findViewById(R.id.scheduleItemRoomType);
        }
    }
}