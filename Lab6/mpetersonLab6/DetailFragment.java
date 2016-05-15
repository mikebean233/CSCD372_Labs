package michaelpeterson.lab6;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DetailFragment extends Fragment {
    private TextView textView;
    private static String lastValue;

    public DetailFragment() {
        // Required empty public constructor
    }

    public void setText(String value){
        if(value == null)
            return;

        if(textView != null)
            textView.setText(value);

        lastValue = value;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setText(lastValue);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        if(rootView != null)
            textView = (TextView)rootView.findViewById(R.id.textViewDetailFrag);

        setText(lastValue);
        return rootView;
    }

}
