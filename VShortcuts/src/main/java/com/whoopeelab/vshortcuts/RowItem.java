package com.whoopeelab.vshortcuts;

import android.graphics.drawable.Drawable;

public class RowItem {

  private String letter;
  private String name;
  private Drawable icon;

  public RowItem(String letter, String name, Drawable icon) {
    this.letter = letter;
    this.name = name;
    this.icon = icon;
  }

  public Drawable getIcon() {
    return icon;
  }
  public void setIcon(Drawable icon) {
    this.icon = icon;
  }
  public String getLetter() {
    return letter;
  }
  public void setLetter(String letter) {
    this.letter = letter;
  }
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
}
