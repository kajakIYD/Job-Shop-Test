/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

/**
 * Custom tuple class, implemented because of lack of tuples in Java
 * @author PF
 * @version 1.0
 * @since   2018-01-03 
 */
public class Tuple<X, Y, Z> { 
  public final X x; 
  public final Y y;
  public final Z z;
  public Tuple(X x, Y y, Z z) { 
    this.x = x; 
    this.y = y; 
    this.z = z;
  } 
} 


