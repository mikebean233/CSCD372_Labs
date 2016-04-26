package michaelpeterson.cscd372.myapplication;

import java.io.Serializable;
import java.util.ArrayList;

public class Manufacturer implements Serializable{
    private String _name;
    private ArrayList<String> _carModels;

    public Manufacturer(String name){
        if(name == null)
            throw new NullPointerException();

        _carModels = new ArrayList<String>();
        _name = name;
    }

    public String getName(){return _name;}

    public String getModelByPosition(int position){return _carModels.get(position);}

    public boolean addModel(String newModel){
        if(newModel == null)
            return false;
        if(!_carModels.contains(newModel)){
            _carModels.add(newModel);
            return true;
        }
        else
            return false;

    }

    public boolean deleteModel(String model){
        if(model == null)
            return false;
        return _carModels.remove(model);
    }

    public boolean containsModel(String model){
        if(model == null)
            return false;
        return _carModels.contains(model);
    }

    public int getModelCount(){return _carModels.size();}

    public int addModels(String[] models){
        if(models == null || models.length == 0)
            return 0;

        int successfullAddCount = 0;
        for(int i = 0; i < models.length; ++i)
            successfullAddCount += (addModel(models[i])) ? 1 : 0;

        return successfullAddCount;
    }

    public int addModels(Iterable<String> models){
        if(models == null)
            return 0;

        int successfulAddCount = 0;
        for(String thisModel : models)
            successfulAddCount += (addModel(thisModel)) ? 1 : 0;

        return successfulAddCount;
    }

    @Override
    public boolean equals(Object that){
        if(!(that instanceof Manufacturer))
            return false;
        Manufacturer thatManufacturer = (Manufacturer) that;
        return this._name.equals(thatManufacturer._name);
    }

    @Override
    public String toString(){return _name;}
}
