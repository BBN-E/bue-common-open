package com.bbn.nlp.corpora.ere;

import com.google.common.base.Optional;

import java.util.Objects;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;


public final class EREFillerArgument implements EREArgument {

  private final String role;
  @Nullable
  private final LinkRealis realis;
  private final EREFiller filler;

  private EREFillerArgument(final String role, @Nullable final LinkRealis realis,
      final EREFiller filler) {
    this.realis = realis;
    this.role = checkNotNull(role);
    this.filler = checkNotNull(filler);
  }

  public static EREFillerArgument from(final String role, final EREFiller filler) {
    return from(role, null, filler);
  }

  public static EREFillerArgument from(final String role, @Nullable final LinkRealis realis,
      final EREFiller filler) {
    return new EREFillerArgument(role, realis, filler);
  }

  public EREFiller filler() {
    return filler;
  }

  @Override
  public ERESpan getExtent() {
    return filler().getExtent();
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
  public Optional<LinkRealis> getRealis() {
    return Optional.fromNullable(realis);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final EREFillerArgument that = (EREFillerArgument) o;
    return Objects.equals(role, that.role) &&
        Objects.equals(realis, that.realis) &&
        Objects.equals(filler, that.filler);
  }

  @Override
  public int hashCode() {
    return Objects.hash(role, realis, filler);
  }
}
