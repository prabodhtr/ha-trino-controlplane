package trino.common.models;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public abstract class Subject<T> implements Observable{

    @Getter
    @Setter
    protected T state;

    List<Observer> observers;

    protected Subject(){
        observers = new ArrayList<>();
    }

    public void addObserver(Observer observer){
        observers.add(observer);
    }

    public void removeObserver(Observer observer){
        observers.remove(observer);
    }

    public void notifyObservers(){
        if(observers != null){
            observers.forEach(observer -> observer.update());
        }
    }

}
