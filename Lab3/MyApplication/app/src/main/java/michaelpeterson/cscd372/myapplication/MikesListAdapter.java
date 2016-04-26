package michaelpeterson.cscd372.myapplication;

import android.app.Activity;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MikesListAdapter extends BaseExpandableListAdapter implements View.OnClickListener {
    private Activity _creator;
    private ArrayList<Manufacturer> _manufacturers;

    public MikesListAdapter(Activity creator, Iterable<Manufacturer> manufacturers){
        super();
        _creator = creator;

        _manufacturers = new ArrayList<Manufacturer>();

        if(manufacturers != null)
            for(Manufacturer thisManufacturer : manufacturers)
                if(!_manufacturers.contains(thisManufacturer))
                    _manufacturers.add(thisManufacturer);

    }


    private Manufacturer getManufacturerAt(int groupPosition){
        if(_manufacturers.size() <= groupPosition)
            throw new IllegalArgumentException("Invalid group position, there is no such group at index " + groupPosition);

        return _manufacturers.get(groupPosition);
    }

    private String getModelAt(Manufacturer manufacturer, int childPosition) {
        if (manufacturer == null)
            throw new NullPointerException();

        try {
            return manufacturer.getModelByPosition(childPosition);
        } catch (Exception ex) {
            throw new IllegalArgumentException("The manufacturer " + manufacturer + " does not contain a model at index " + childPosition);
        }
    }

    @Override
    public int getGroupCount() {
        return _manufacturers.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return getManufacturerAt(groupPosition).getModelCount();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return getManufacturerAt(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        Manufacturer thisManufacturer = getManufacturerAt(groupPosition);
        return getModelAt(thisManufacturer, childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return (long) childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        Manufacturer thisManufacturer = getManufacturerAt(groupPosition);

        View thisView = (convertView != null) ? convertView : _creator.getLayoutInflater().inflate(R.layout.group_item_layout, parent, false);

        TextView manufacturerDescription = ((TextView) thisView.findViewById(R.id.textView));
        manufacturerDescription.setText(thisManufacturer.getName());
        return thisView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final Manufacturer thisManufacturer = getManufacturerAt(groupPosition);
        final String thisModel = getModelAt(thisManufacturer, childPosition);

        View thisView = (convertView != null) ? convertView : _creator.getLayoutInflater().inflate(R.layout.child_item, parent, false);


        TextView modelDescription = ((TextView) thisView.findViewById(R.id.textView2));
        ImageView deleteImage = ((ImageView) thisView.findViewById(R.id.imageView));

        deleteImage.setOnClickListener(this);

        deleteImage.setTag(R.id.group_num, groupPosition);
        deleteImage.setTag(R.id.child_num, childPosition);

        modelDescription.setText(thisModel);
        return thisView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        try {
            getModelAt(getManufacturerAt(groupPosition), childPosition);
            return true;
        }
        catch(Exception ex){
            return false;
        }
    }

    @Override
    public void onClick(View v) {
        int groupPosition = (Integer)v.getTag(R.id.group_num);
        int childPosition = (Integer)v.getTag(R.id.child_num);

        final Manufacturer thisManufacturer = getManufacturerAt(groupPosition);
        final String thisModel = getModelAt(thisManufacturer, childPosition);
        final Snackbar thisSnackBar = Snackbar.make(v, "Delete " + thisManufacturer + " " + thisModel + " ?", Snackbar.LENGTH_LONG);

        thisSnackBar.setAction("Confirm", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                thisManufacturer.deleteModel(thisModel);
                notifyDataSetChanged();
                thisSnackBar.dismiss();
            }
        }).show();
    }
}
