package es.disatec.ticketreader;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by MiguelLuna on 30/09/2016.
 */
public class MyFragmentPagerAdapter extends FragmentPagerAdapter {
    ArrayList<Ticket> colTickets;
    
    public MyFragmentPagerAdapter(FragmentManager fm, ArrayList<Ticket> colTickets)
    {
        super(fm);
        this.colTickets = colTickets;

    }




  @Override
    public Fragment getItem(int position){
        MyFragment myFragment = new MyFragment();
        Bundle data = new Bundle();
        data.putInt("current_page", position);
        data.putInt("idTicket", colTickets.get(position).getIdticket());
        myFragment.setArguments(data);
        return myFragment;
    }

    @Override
    public int getCount() {
        return colTickets.toArray().length;
    }

    public CharSequence getPageTitle(int position) {
        return "Page #" + ( position + 1 );
    }
}
