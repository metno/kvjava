package no.met.kvutil;

/**
 * Created by borgem on 11.11.16.
 */
public class Tuple2<T1, T2> {
  public T1 _1;
  public T2 _2;

  public Tuple2(T1 t1, T2 t2) {
    this._1 = t1;
    this._2 = t2;
  }

   public static  <T1, T2> Tuple2<T1,T2> of(T1 t1, T2 t2) {
    return new Tuple2(t1, t2);
  }

  @Override
  public String toString() {
    return String.format("(%s, %s)", _1, _2);
  }
}

