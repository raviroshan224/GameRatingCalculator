package com.Centaurii.app.RatingCalculator.fragments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.Centaurii.app.RatingCalculator.GameRatingCalculatorActivity;
import com.Centaurii.app.RatingCalculator.R;
import com.Centaurii.app.RatingCalculator.adapters.ColorAdapter;
import com.Centaurii.app.RatingCalculator.adapters.ProfileListAdapter;
import com.Centaurii.app.RatingCalculator.listeners.AddProfileOnClickListener;
import com.Centaurii.app.RatingCalculator.listeners.GoBackClickListener;
import com.Centaurii.app.RatingCalculator.model.Profile;
import com.Centaurii.app.RatingCalculator.util.Tags;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

public class ProfileViewerFragment extends Fragment implements OnItemClickListener
{
    Resources res;
    ListView profilesList;
    ProfileListAdapter adapter;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) 
    {
        View view = inflater.inflate(R.layout.fragment_profiles, container, false);
        
        res = getActivity().getResources();
        
        Button goBack = (Button) view.findViewById(R.id.back_button);
        goBack.setOnClickListener(new GoBackClickListener(getActivity()));
        
        TextView header = (TextView)view.findViewById(R.id.profile_header);
        header.setText(res.getString(R.string.profiles));
        
        Button addProfile = (Button)view.findViewById(R.id.add_profile);
        addProfile.setOnClickListener(
                new AddProfileOnClickListener((GameRatingCalculatorActivity)getActivity()));
        
        ArrayList<Profile> profiles = ((GameRatingCalculatorActivity) getActivity()).getSavedProfiles();
        
        if(profiles != null)
        {
            Collections.sort(profiles);
        
            profilesList = (ListView)view.findViewById(R.id.profiles_list);
            adapter = new ProfileListAdapter(getActivity(), R.layout.profile_list_segment, profiles);
            
            profilesList.setAdapter(adapter);
            profilesList.setOnItemClickListener(this);
        }
        else
        {
            Intent intent = getActivity().getIntent();
            getActivity().startActivity(intent);
        }
        
        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        DeleteListener listener = new DeleteListener((GameRatingCalculatorActivity) getActivity(), position);
        
        LayoutInflater inflater = getActivity().getLayoutInflater();
        
        View playerOptions = inflater.inflate(R.layout.player_options, null, false);
        
        ((TextView) playerOptions.findViewById(R.id.player_options)).setText(res.getString(R.string.pressed_player_dialog));
        
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(playerOptions)
               .setPositiveButton(res.getString(R.string.delete), listener)
               .setNeutralButton(res.getString(R.string.edit_profile), listener)
               .setNegativeButton(res.getString(R.string.cancel), listener)
               .create()
               .show();
    }
    
    public void notifyAdapter()
    {
        adapter.notifyDataSetChanged();
    }
    
    private class DeleteListener implements DialogInterface.OnClickListener
    {
        GameRatingCalculatorActivity act;
        int position;
        
        public DeleteListener(GameRatingCalculatorActivity act, int position)
        {
            this.act = act;
            this.position = position;
        }
        
        @Override
        public void onClick(DialogInterface dialog, int id)
        {
            switch(id)
            {
                case Dialog.BUTTON_POSITIVE:
                    act.getSavedProfiles().remove(position);
                    ProfileViewerFragment.this.adapter.notifyDataSetChanged();
                    break;
                case Dialog.BUTTON_NEUTRAL:
                    HashMap<String, Integer> map = Tags.getColorMap();
                    Set<String> colorSet = Tags.getColorMap().keySet();
                    ArrayList<String> colorList = new ArrayList<String>(colorSet);
                    ColorAdapter colorAdapter = new ColorAdapter(getActivity(), R.layout.color_spinner, colorList);
                    
                    AlertDialog.Builder builder = new AlertDialog.Builder(act);
                    
                    LayoutInflater inflater = act.getLayoutInflater();
                    
                    Profile temp = act.getSavedProfiles().get(position);
                    
                    View playerEditView = inflater.inflate(R.layout.fragment_add_profile, null, false);
                    
                    final TextView header = (TextView) playerEditView.findViewById(R.id.add_profile_header);
                    header.setText(res.getString(R.string.edit_profile));
                    
                    final EditText name = (EditText) playerEditView.findViewById(R.id.profile_name);
                    name.setText(temp.getName());
                    
                    final EditText rating = (EditText) playerEditView.findViewById(R.id.profile_rating);
                    rating.setText("" + temp.getRating());
                    rating.setHint(res.getString(R.string.rating) + " (Default is " + GameRatingCalculatorActivity.DEFAULT_RATING() + ")");
                    
                    final Spinner colorSpinner = (Spinner) playerEditView.findViewById(R.id.profile_color);
                    colorSpinner.setAdapter(colorAdapter);
                    
                    Log.i("ProfView", "" + "index of color: " + temp.getFavColor());
                    
                    String colorName = "White";
                    for(Map.Entry<String, Integer> entry: map.entrySet())
                    {
                        if(entry.getValue().equals(temp.getFavColor()))
                        {
                            colorName = entry.getKey();
                            break;
                        }
                    }
                        
                    
                    colorSpinner.setSelection(colorList.indexOf(colorName));
                    
                    final CheckBox provisional = (CheckBox) playerEditView.findViewById(R.id.profile_provisional);
                    final EditText numProvGames = (EditText) playerEditView.findViewById(R.id.num_prov_games);
                    numProvGames.setHint(res.getString(R.string.prov_games_option) + " (Default is " + GameRatingCalculatorActivity.DEFAULT_PROVISIONAL() + ")");
                    if(temp.isProvisional())
                    {
                        provisional.setChecked(true);
                        numProvGames.setVisibility(View.VISIBLE);
                        numProvGames.setText("" + temp.getProvisionalGamesLeft());
                    }
                    final LinearLayout myCheckBox = (LinearLayout) playerEditView.findViewById(R.id.my_check_box);
                    myCheckBox.setOnClickListener(new OnClickListener()
                    {

                        @Override
                        public void onClick(View view)
                        {
                            provisional.toggle();
                            if(provisional.isChecked())
                            {
                                numProvGames.setVisibility(View.VISIBLE);
                            }
                            else
                            {
                                numProvGames.setVisibility(View.GONE);
                            }
                        }
                        
                    });
                    
                    EditListener listener = new EditListener(temp, name, rating, colorSpinner, provisional,
                            numProvGames, adapter);
                    
                    builder.setView(playerEditView)
                           .setPositiveButton(res.getString(R.string.edit), listener)
                           .setNegativeButton(res.getString(R.string.cancel), null)
                           .create()
                           .show();
                    break;
                case Dialog.BUTTON_NEGATIVE:
                    break;
            }
        }
        
    }
    
    private class EditListener implements DialogInterface.OnClickListener
    {
        Profile prof;
        EditText name, rating, numProvGames;
        Spinner colorSpinner;
        CheckBox provisional;
        ArrayAdapter<Profile> adapter;
        
        public EditListener(Profile prof, EditText name, EditText rating, Spinner colorSpinner,
                CheckBox provisional, EditText numProvGames, ArrayAdapter<Profile> adapter)
        {
            this.prof = prof;
            this.name = name;
            this.rating = rating;
            this.colorSpinner = colorSpinner;
            this.provisional = provisional;
            this.numProvGames = numProvGames;
            this.adapter = adapter;
        }
        
        @Override
        public void onClick(DialogInterface arg0, int arg1)
        {
            //try-catch statements protect from empty strings
            prof.setName(name.getText().toString());
            try
            {
                prof.setRating(Integer.valueOf(rating.getText().toString()));
            }
            catch(Exception e)
            {
                prof.setRating(GameRatingCalculatorActivity.DEFAULT_RATING());
            }
            prof.setFavColor(Tags.getColorMap().get(colorSpinner.getSelectedItem()));
            prof.setProvisional(provisional.isChecked());
            try
            {
                int temp = Integer.valueOf(numProvGames.getText().toString());
                if(temp < 1)
                {
                    throw new Exception();
                }
                else
                {
                    prof.setProvisionalGamesLeft(provisional.isChecked() ? temp : 0);
                }
            }
            catch (Exception e)
            {
                prof.setProvisionalGamesLeft(provisional.isChecked() ? GameRatingCalculatorActivity.DEFAULT_PROVISIONAL() : 0);
            }
            adapter.notifyDataSetChanged();
        }
        
    }
}
