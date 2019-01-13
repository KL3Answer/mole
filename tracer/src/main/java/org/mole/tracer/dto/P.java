/*
 * Copyright (c) 2010, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package org.mole.tracer.dto;

import javafx.beans.NamedArg;

import java.io.Serializable;

 /**
  * <p>A convenience class to represent name-_1 pairs.</p>
  * @since JavaFX 2.0
  */
public class P<K,V> implements Serializable{

    /**
     * Key of this <code>P</code>.
     */
    private K _0;

    /**
     * Gets the _0 for this pair.
     * @return _0 for this pair
     */
    public K get0() { return _0; }

    /**
     * Value of this this <code>P</code>.
     */
    private V _1;

    /**
     * Gets the _1 for this pair.
     * @return _1 for this pair
     */
    public V get1() { return _1; }

    /**
     * Creates a new pair
     * @param _0 The _0 for this pair
     * @param _1 The _1 to use for this pair
     */
    public P(@NamedArg("_0") K _0, @NamedArg("_1") V _1) {
        this._0 = _0;
        this._1 = _1;
    }

    /**
     * <p><code>String</code> representation of this
     * <code>P</code>.</p>
     *
     * <p>The default name/_1 delimiter '=' is always used.</p>
     *
     *  @return <code>String</code> representation of this <code>P</code>
     */
    @Override
    public String toString() {
        return _0 + "=" + _1;
    }

    /**
     * <p>Generate a hash code for this <code>P</code>.</p>
     *
     * <p>The hash code is calculated using both the name and
     * the _1 of the <code>P</code>.</p>
     *
     * @return hash code for this <code>P</code>
     */
    @Override
    public int hashCode() {
        // name's hashCode is multiplied by an arbitrary prime number (13)
        // in order to make sure there is a difference in the hashCode between
        // these two parameters:
        //  name: a  _1: aa
        //  name: aa _1: a
        return _0.hashCode() * 13 + (_1 == null ? 0 : _1.hashCode());
    }

     /**
      * <p>Test this <code>P</code> for equality with another
      * <code>Object</code>.</p>
      *
      * <p>If the <code>Object</code> to be tested is not a
      * <code>P</code> or is <code>null</code>, then this method
      * returns <code>false</code>.</p>
      *
      * <p>Two <code>P</code>s are considered equal if and only if
      * both the names and values are equal.</p>
      *
      * @param o the <code>Object</code> to test for
      * equality with this <code>P</code>
      * @return <code>true</code> if the given <code>Object</code> is
      * equal to this <code>P</code> else <code>false</code>
      */
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o instanceof P) {
             P pair = (P) o;
             if (_0 != null ? !_0.equals(pair._0) : pair._0 != null) return false;
             if (_1 != null ? !_1.equals(pair._1) : pair._1 != null) return false;
             return true;
         }
         return false;
     }
 }

