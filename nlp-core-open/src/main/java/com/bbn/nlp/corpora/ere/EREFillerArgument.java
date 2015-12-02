package com.bbn.nlp.corpora.ere;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Objects;


public final class EREFillerArgument implements EREArgument {
  private final String role;
  private final EREFiller filler;
    
  private EREFillerArgument(final String role, final EREFiller filler) {
    this.role = checkNotNull(role);
    this.filler = checkNotNull(filler);
  }
    
  public static EREFillerArgument from(final String role, final EREFiller filler) {
    return new EREFillerArgument(role, filler);
  }
    
  public EREFiller filler() {
    return filler;
  }

  @Override
  public String getID() {
    return filler.getID();
  }
  
  @Override
  public String getRole() {
    return role;
  }
  
  @Override
  public int hashCode() {
    return Objects.hashCode(role, filler);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final EREFillerArgument other = (EREFillerArgument) obj;
    return Objects.equal(role, other.role) && 
        Objects.equal(filler, other.filler);
  }
  
}
