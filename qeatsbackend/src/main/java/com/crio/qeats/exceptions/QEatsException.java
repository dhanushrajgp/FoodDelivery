
package com.crio.qeats.exceptions;

abstract class QEatsException extends RuntimeException {

  public static final int EMPTY_CART = 100;
  public static final int ITEM_NOT_FOUND_IN_RESTAURANT_MENU = 101;
  public static final int ITEM_NOT_FROM_SAME_RESTAURANT = 102;
  public static final int CART_NOT_FOUND = 103;

  QEatsException() {}

  QEatsException(String message) {
    super(message);
  }

  public abstract int getErrorType();

}
