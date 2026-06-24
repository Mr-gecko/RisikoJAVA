package model.network;

// functional interface
public interface Request<T>{
	public void handle(T ctx);
	public String toString();
}





