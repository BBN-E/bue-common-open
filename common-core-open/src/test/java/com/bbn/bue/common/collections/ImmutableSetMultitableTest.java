package com.bbn.bue.common.collections;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Multimap;

import org.junit.Test;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests methods in {@link ImmutableSetMultitable}
 */
public class ImmutableSetMultitableTest {

  @Test
  public void testContainsImmutableSet() {
    ImmutableSetMultitable<Integer, Character, String> table =
        ImmutableSetMultitable.<Integer, Character, String>builder()
            .put(3, 'a', "foo")
            .build();

    final Collection<String> set = table.get(3, 'a');
    assertEquals(ImmutableSet.of("foo"), set);
    assertTrue(set instanceof ImmutableSet);
  }

  @Test
  public void testEquality() {
    // test insertion order and duplicates don't matter for equality
    ImmutableSetMultitable<Integer, Character, String> myTable1 =
        ImmutableSetMultitable.<Integer, Character, String>builder()
            .put(3, 'a', "all")
            .put(3, 'b', "bad")
            .put(4, 'b', "bass")
            .put(4, 'b', "bass")
            .build();

    ImmutableSetMultitable<Integer, Character, String> myTable2 =
        ImmutableSetMultitable.<Integer, Character, String>builder()
            .put(4, 'b', "bass")
            .put(3, 'b', "bad")
            .put(3, 'a', "all")
            .build();
    assertEquals(myTable1, myTable2);

    //test empty multitables are equal
    ImmutableMultitable<Integer, Character, String> mySetTable =
        ImmutableSetMultitable.<Integer, Character, String>builder().build();
    ImmutableMultitable<Integer, Character, String> myListTable =
        ImmutableListMultitable.<Integer, Character, String>builder().build();
    assertEquals(mySetTable, myListTable);
  }

  @Test
  public void testDuplicates() {
    //check that set eliminates duplicate values
    ImmutableSetMultitable<Integer, Character, String> myTable =
        ImmutableSetMultitable.<Integer, Character, String>builder()
            .put(3, 'a', "all")
            .put(3, 'a', "all")
            .put(3, 'b', "bad")
            .put(4, 'b', "bass")
            .put(4, 'b', "bass")
            .build();
    assertEquals(3, myTable.size());
  }

  @Test
  public void getAsSet() {
    //create table and sets to check against
    ImmutableSetMultitable<Integer, Character, String> myTable = createTestTable();
    ImmutableSet<String> setA = ImmutableSet.of("all", "ale");
    ImmutableSet<String> setB = ImmutableSet.of("bar");
    ImmutableSet<String> setDuplicates = ImmutableSet.of("bass");
    ImmutableSet<String> setC = ImmutableSet.of("crawl");
    ImmutableSet<String> emptySet = ImmutableSet.of();

    //check that gets the expected sets
    assertEquals(setA, myTable.getAsSet(3, 'a'));
    assertEquals(setB, myTable.getAsSet(3, 'b'));
    assertEquals(setB, myTable.getAsSet(3, 'c'));
    assertEquals(setDuplicates, myTable.getAsSet(4, 'b'));
    assertEquals(setC, myTable.getAsSet(5, 'c'));

    //check empty cells
    assertEquals(emptySet, myTable.getAsSet(4, 'a'));
    assertEquals(emptySet, myTable.getAsSet(4, 'c'));
    assertEquals(emptySet, myTable.getAsSet(5, 'a'));
    assertEquals(emptySet, myTable.getAsSet(5, 'b'));
    assertEquals(emptySet, myTable.getAsSet(6, 'a'));
    assertEquals(emptySet, myTable.getAsSet(6, 'b'));
    assertEquals(emptySet, myTable.getAsSet(6, 'c'));

    //check keys that aren't present
    assertEquals(myTable.getAsSet(3, 'n'), emptySet);
    assertEquals(myTable.getAsSet(17, 'Z'), emptySet);

    //check empty table
    ImmutableSetMultitable<Integer, Character, String> multitable2 =
        ImmutableSetMultitable.<Integer, Character, String>builder().build();
    assertEquals(multitable2.getAsSet(3, 'a'), emptySet);
  }

  @Test
  public void rowMap() {
    ImmutableMultitable<Integer, Character, String> myTable = createTestTable();
    final Map<Integer, Multimap<Character, String>> myRowMap = myTable.rowMap();

    //test rowMap.get(3)
    final Multimap<Character, String> myMultimap3 = myRowMap.get(3);
    assertEquals(ImmutableSet.of("all", "ale"), myMultimap3.get('a'));
    assertEquals(ImmutableSet.of("bar"), myMultimap3.get('b'));
    assertEquals(ImmutableSet.of("bar"), myMultimap3.get('c'));

    //assert can't alter rowMap
    try {
      myRowMap.put(6, ImmutableMultimap.of('a', "apple"));
      assertFalse(myTable.get(6, 'a').contains("apple"));
      fail();
    } catch (UnsupportedOperationException expected){
      // Correct behavior
    }
    //test RowMap.get(4) duplicates of "bass"
    final Multimap<Character, String> myMultimap4 = myRowMap.get(4);
    assertEquals(ImmutableSet.of("bass"), myMultimap4.get('b'));

    //test RowMap.get(5)
    final Multimap<Character, String> myMultimap5 = myRowMap.get(5);
    assertFalse(myMultimap5.containsKey('a'));
    assertFalse(myMultimap5.containsKey('b'));
    assertEquals(ImmutableSet.of("crawl"), myMultimap5.get('c'));
    assertNull(myRowMap.get(6));

    //test empty table
    final ImmutableSetMultitable<Integer, Character, String> emptyTable =
        ImmutableSetMultitable.<Integer, Character, String>builder().build();
    assertTrue(emptyTable.rowMap().isEmpty());
    assertNull(emptyTable.rowMap().get(1));
  }

  @Test
  public void columnMap() {
    ImmutableMultitable<Integer, Character, String> myTable = createTestTable();
    final Map<Character, Multimap<Integer, String>> myColumnMap = myTable.columnMap();

    //test columnMap.get('a')
    final Multimap<Integer, String> myMultimapA = myColumnMap.get('a');
    assertEquals(ImmutableSet.of("all", "ale"), myMultimapA.get(3));
    assertTrue(myMultimapA.get(4).isEmpty());
    assertTrue(myMultimapA.get(5).isEmpty());

    //assert can't alter rowMap
    try {
      myColumnMap.put('a', ImmutableMultimap.of(3, "apple"));
      assertFalse(myTable.get(6, 'a').contains("apple"));
      fail();
    } catch (UnsupportedOperationException expected){
      // Correct behavior
    }
    //test ColumnMap.get('b') duplicates of "bass"
    final Multimap<Integer, String> myMultimap4 = myColumnMap.get('b');
    assertEquals(ImmutableSet.of("bar"), myMultimap4.get(3));
    assertEquals(ImmutableSet.of("bass"), myMultimap4.get(4));

    //test RowMap.get('c')
    final Multimap<Integer, String> myMultimap5 = myColumnMap.get('c');
    assertFalse(myMultimap5.containsKey(4));
    assertFalse(myMultimap5.containsKey(6));
    assertEquals(ImmutableSet.of("crawl"), myMultimap5.get(5));
    assertNull(myColumnMap.get('d'));

    //test empty table
    final ImmutableSetMultitable<Integer, Character, String> emptyTable =
        ImmutableSetMultitable.<Integer, Character, String>builder().build();
    assertTrue(emptyTable.columnMap().isEmpty());
    assertNull(emptyTable.columnMap().get('a'));
  }

  @Test
  public void values() {
    //empty case
    ImmutableSetMultitable<Integer, Character, String> myTable0 =
        ImmutableSetMultitable.<Integer, Character, String>builder().build();
    assertTrue(myTable0.values().isEmpty());

    //single case
    ImmutableSetMultitable<Integer, Character, String> myTable1 =
        ImmutableSetMultitable.<Integer, Character, String>builder()
            .put(3, 'a', "all")
            .build();
    assertEquals(1, myTable1.values().size());
    assertTrue(myTable1.values().contains("all"));

    //two
    ImmutableSetMultitable<Integer, Character, String> myTable2 =
        ImmutableSetMultitable.<Integer, Character, String>builder()
            .put(3, 'a', "all")
            .put(4, 'b', "bat")
            .put(4, 'b', "bat")
            .put(5, 'b', "bat")
            .build();
    //check that for same row-col eliminates duplicates, but not for different cell
    assertEquals(3, myTable2.values().size());
    assertEquals(ImmutableMultiset.of("all", "bat", "bat"), myTable2.values());
    assertNotEquals(ImmutableMultiset.of("all", "bat", "bat", "bat"), myTable2.values());
    assertFalse(myTable2.values().contains("cat"));
  }

  @Test
  public void contains() {
    //check contains
    ImmutableMultitable<Integer, Character, String> myTable = createTestTable();
    assertTrue(myTable.contains(3, 'a'));
    assertTrue(myTable.contains(3, 'b'));
    assertTrue(myTable.contains(3, 'c'));
    assertTrue(myTable.contains(4, 'b'));
    assertTrue(myTable.contains(5, 'c'));
    //check doesn't contain
    assertFalse(myTable.contains(4, 'a'));
    assertFalse(myTable.contains(4, 'c'));
    assertFalse(myTable.contains(5, 'a'));
    assertFalse(myTable.contains(5, 'b'));
    assertFalse(myTable.contains(6, 'd'));
    assertFalse(myTable.contains(3, 'd'));
    //check against objects that don't belong
    assertFalse(myTable.contains(3.0, 'a'));
    assertFalse(myTable.contains(3.1, 'a'));
    assertFalse(myTable.contains("kittens", 'a'));
    assertFalse(myTable.contains(3, "a"));
  }

  @Test
  public void containsRow() {
    //empty case
    ImmutableSetMultitable<Integer, Character, String> myTable0 =
        ImmutableSetMultitable.<Integer, Character, String>builder().build();
    assertFalse(myTable0.containsRow(3));
    assertFalse(myTable0.containsRow('b'));

    //single case
    ImmutableSetMultitable<Integer, Character, String> myTable1 =
        ImmutableSetMultitable.<Integer, Character, String>builder()
            .put(3, 'a', "all")
            .build();
    assertTrue(myTable1.containsRow(3));
    assertFalse(myTable1.containsRow(4));

    //more than one
    ImmutableMultitable<Integer, Character, String> myTable2 = createTestTable();
    assertTrue(myTable2.containsRow(3));
    assertTrue(myTable2.containsRow(4));
    assertTrue(myTable2.containsRow(5));
    assertFalse(myTable2.containsRow(2));
    assertFalse(myTable2.containsRow(6));
    assertFalse(myTable2.containsRow('b'));
  }

  @Test
  public void containsColumn() {
    //empty case
    ImmutableSetMultitable<Integer, Character, String> myTable0 =
        ImmutableSetMultitable.<Integer, Character, String>builder().build();
    assertFalse(myTable0.containsColumn(3));
    assertFalse(myTable0.containsColumn('c'));

    //single case
    ImmutableSetMultitable<Integer, Character, String> myTable1 =
        ImmutableSetMultitable.<Integer, Character, String>builder()
            .put(3, 'a', "all")
            .build();
    assertTrue(myTable1.containsColumn('a'));
    assertFalse(myTable1.containsColumn(4));
    assertFalse(myTable1.containsColumn('b'));

    //many
    ImmutableMultitable<Integer, Character, String> myTable2 = createTestTable();
    assertTrue(myTable2.containsColumn('a'));
    assertTrue(myTable2.containsColumn('b'));
    assertTrue(myTable2.containsColumn('c'));
    assertFalse(myTable2.containsColumn('d'));
    assertFalse(myTable2.containsColumn(3));
  }

  @Test
  public void containsValue() {
    //empty case
    ImmutableSetMultitable<Integer, Character, String> myTable0 =
        ImmutableSetMultitable.<Integer, Character, String>builder().build();
    assertFalse(myTable0.containsValue("all"));
    assertFalse(myTable0.containsValue("bar"));
    //one
    ImmutableSetMultitable<Integer, Character, String> myTable1 =
        ImmutableSetMultitable.<Integer, Character, String>builder()
            .put(3, 'a', "all")
            .build();
    assertTrue(myTable1.containsValue("all"));
    assertFalse(myTable1.containsValue("bar"));
    //many
    ImmutableMultitable<Integer, Character, String> myTable2 = createTestTable();
    assertTrue(myTable2.containsValue("all"));
    assertTrue(myTable2.containsValue("ale"));
    assertTrue(myTable2.containsValue("bar"));
    assertTrue(myTable2.containsValue("bass"));
    assertFalse(myTable2.containsValue(""));
    assertFalse(myTable2.containsValue(" "));
    assertFalse(myTable2.containsValue("cat"));
  }

  @Test
  public void isEmpty() {
    //empty case
    ImmutableSetMultitable<Integer, Character, String> myTable0 =
        ImmutableSetMultitable.<Integer, Character, String>builder().build();
    assertTrue(myTable0.isEmpty());
    //single case
    ImmutableSetMultitable<Integer, Character, String> myTable1 =
        ImmutableSetMultitable.<Integer, Character, String>builder()
            .put(3, 'a', "all")
            .build();
    assertFalse(myTable1.isEmpty());
    //many
    ImmutableSetMultitable<Integer, Character, String> myTable2 =
        ImmutableSetMultitable.<Integer, Character, String>builder()
            .putAll(3, 'a', ImmutableList.of("all", "ale"))
            .putAll(4, 'b', ImmutableList.of("bass", "bass"))
            .build();
    assertFalse(myTable2.isEmpty());
  }

  @Test
  public void size() {
    //empty case
    ImmutableSetMultitable<Integer, Character, String> myTable0 =
        ImmutableSetMultitable.<Integer, Character, String>builder().build();
    assertEquals(0, myTable0.size());

    //single case
    ImmutableSetMultitable<Integer, Character, String> myTable1 =
        ImmutableSetMultitable.<Integer, Character, String>builder()
            .put(3, 'a', "all")
            .build();
    assertEquals(1, myTable1.size());

    //single case w/duplicate
    ImmutableSetMultitable<Integer, Character, String> myTable1Duplicate =
        ImmutableSetMultitable.<Integer, Character, String>builder()
            .put(3, 'a', "all")
            .put(3, 'a', "all")
            .build();
    assertEquals(1, myTable1Duplicate.size());

    //two
    ImmutableSetMultitable<Integer, Character, String> myTable2 =
        ImmutableSetMultitable.<Integer, Character, String>builder()
            .put(3, 'a', "all")
            .put(4, 'b', "bat")
            .build();
    assertEquals(2, myTable2.size());

    //many, tests duplicates in table
    ImmutableMultitable<Integer, Character, String> myTable3 = createTestTable();
    assertNotEquals(7, myTable3.size());
    assertEquals(6, myTable3.size());
  }

  @Test
  public void putAll() {
    //check putAll for both list and set collections
    ImmutableSet<String> setA = ImmutableSet.of("all", "ale", "bat");
    ImmutableList<String> listB = ImmutableList.of("bat", "bat");
    ImmutableSetMultitable<Integer, Character, String> myTable3 =
        ImmutableSetMultitable.<Integer, Character, String>builder()
            .putAll(3, 'a', setA)
            .putAll(4, 'b', listB)
            .build();
    //double check size again to ensure duplicates in a cell are not counted.
    assertEquals(4, myTable3.size());
    assertNotEquals(5, myTable3.size());
  }

  @Test
  public void rowKeySet() {
    //empty case
    ImmutableSetMultitable<Integer, Character, String> myTable0 =
        ImmutableSetMultitable.<Integer, Character, String>builder().build();
    assertTrue(myTable0.rowKeySet().isEmpty());

    //single case
    ImmutableSetMultitable<Integer, Character, String> myTable1 =
        ImmutableSetMultitable.<Integer, Character, String>builder()
            .put(3, 'a', "all")
            .build();
    assertEquals(1, myTable1.rowKeySet().size());
    assertTrue(myTable1.rowKeySet().contains(3));

    //two
    ImmutableSetMultitable<Integer, Character, String> myTable2 =
        ImmutableSetMultitable.<Integer, Character, String>builder()
            .put(3, 'a', "all")
            .put(4, 'b', "bat")
            .build();
    assertEquals(2, myTable2.rowKeySet().size());
    assertTrue(myTable2.rowKeySet().contains(3));
    assertTrue(myTable2.rowKeySet().contains(4));
    assertFalse(myTable2.rowKeySet().contains(5));
  }

  @Test
  public void columnKeySet() {
    //empty case
    ImmutableSetMultitable<Integer, Character, String> myTable0 =
        ImmutableSetMultitable.<Integer, Character, String>builder().build();
    assertTrue(myTable0.columnKeySet().isEmpty());

    //single case
    ImmutableSetMultitable<Integer, Character, String> myTable1 =
        ImmutableSetMultitable.<Integer, Character, String>builder()
            .put(3, 'a', "all")
            .build();
    assertEquals(1, myTable1.rowKeySet().size());
    assertTrue(myTable1.columnKeySet().contains('a'));

    //two
    ImmutableSetMultitable<Integer, Character, String> myTable2 =
        ImmutableSetMultitable.<Integer, Character, String>builder()
            .put(3, 'a', "all")
            .put(4, 'b', "bat")
            .build();
    assertEquals(2, myTable2.rowKeySet().size());
    assertTrue(myTable2.columnKeySet().contains('a'));
    assertTrue(myTable2.columnKeySet().contains('b'));
    assertFalse(myTable2.columnKeySet().contains('c'));
  }

  @Test
  public void get() {
    //empty case
    ImmutableSetMultitable<Integer, Character, String> myTable0 =
        ImmutableSetMultitable.<Integer, Character, String>builder().build();
    assertTrue(myTable0.get(3, 'a').isEmpty());

    //single case
    ImmutableSetMultitable<Integer, Character, String> myTable1 =
        ImmutableSetMultitable.<Integer, Character, String>builder()
            .put(3, 'a', "all")
            .build();
    assertEquals(ImmutableSet.of("all"), myTable1.get(3, 'a'));

    //two
    ImmutableSetMultitable<Integer, Character, String> myTable2 =
        ImmutableSetMultitable.<Integer, Character, String>builder()
            .put(3, 'a', "all")
            .put(3, 'a', "ale")
            .put(4, 'b', "bat")
            .put(4, 'b', "bat")
            .build();
    assertEquals(ImmutableSet.of("all", "ale"), myTable2.get(3, 'a'));
    assertEquals(ImmutableSet.of("bat"), myTable2.get(4, 'b'));
    assertTrue(myTable2.get(3, 'b').isEmpty());
    assertTrue(myTable2.get(1000, 'd').isEmpty());
  }

  @Test
  public void row() {
    //empty case
    ImmutableSetMultitable<Integer, Character, String> myTable0 =
        ImmutableSetMultitable.<Integer, Character, String>builder().build();
    assertTrue(myTable0.row(1).isEmpty());

    //check immutability (guaranteed because returned maps are immutable)
    try {
      Multimap<Character, String> myMap = myTable0.row(1);
      myMap.putAll('a', ImmutableSet.of("apple", "add"));
    fail();
    } catch(UnsupportedOperationException expected) {
    //correct behavior
    }
    //single case
    ImmutableSetMultitable<Integer, Character, String> myTable1 =
        ImmutableSetMultitable.<Integer, Character, String>builder()
            .put(3, 'a', "all")
            .build();
    assertEquals(ImmutableSetMultimap.of('a', "all"), myTable1.row(3));
    assertTrue(myTable1.row(4).isEmpty());
    assertTrue(myTable1.row(3).get('a').contains("all"));

    //two
    ImmutableSetMultitable<Integer, Character, String> myTable2 =
        ImmutableSetMultitable.<Integer, Character, String>builder()
            .put(3, 'a', "all")
            .put(3, 'b', "bad")
            .put(4, 'b', "bass")
            .put(4, 'b', "bass")
            .build();
    //check rows
    assertEquals(ImmutableSetMultimap.of('a', "all", 'b', "bad"),
        myTable2.row(3));
    assertEquals(ImmutableSetMultimap.of('b', "bass"), myTable2.row(4));
    //check empty
    assertTrue(myTable2.row(6).isEmpty());
  }

  @Test
  public void column() {
    //empty case
    ImmutableSetMultitable<Integer, Character, String> myTable0 =
        ImmutableSetMultitable.<Integer, Character, String>builder().build();
    assertTrue(myTable0.column('a').isEmpty());
    //check immutability (guaranteed because returned maps are immutable)
    try {
      Multimap<Integer, String> myMap = myTable0.column('a');
      myMap.putAll(1, ImmutableSet.of("apple", "add"));
      fail();
    } catch(UnsupportedOperationException expected) {
      //correct behavior
    }
    //single case
    ImmutableSetMultitable<Integer, Character, String> myTable1 =
        ImmutableSetMultitable.<Integer, Character, String>builder()
            .put(3, 'a', "all")
            .build();
    assertEquals(ImmutableSetMultimap.of(3, "all"), myTable1.column('a'));
    assertTrue(myTable1.column('b').isEmpty());

    //two
    ImmutableSetMultitable<Integer, Character, String> myTable2 =
        ImmutableSetMultitable.<Integer, Character, String>builder()
            .put(3, 'a', "all")
            .put(3, 'b', "bad")
            .put(4, 'b', "bass")
            .put(4, 'b', "bass")
            .build();
    //check columns
    assertEquals(ImmutableSetMultimap.of(3, "all"),
        myTable2.column('a'));
    assertEquals(ImmutableSetMultimap.of(3, "bad", 4, "bass" ), myTable2.column('b'));
    //check empty
    assertTrue(myTable2.column('c').isEmpty());
  }

  @Test
  public void cellSet() {
    //empty case
    ImmutableSetMultitable<Integer, Character, String> myTable0 =
        ImmutableSetMultitable.<Integer, Character, String>builder().build();
    assertTrue(myTable0.cellSet().isEmpty());

    //single case
    ImmutableSetMultitable<Integer, Character, String> myTable1 =
        ImmutableSetMultitable.<Integer, Character, String>builder()
            .put(3, 'a', "all")
            .build();
    Set<Multitable.Multicell<Integer, Character, String>> cellSet = myTable1.cellSet();

    ImmutableSetMultitable.SetMulticell<Integer, Character, String> cell1 =
        createTestCell(3, 'a', "all");
    ImmutableSetMultitable.SetMulticell<Integer, Character, String> cell2 =
        createTestCell(4, 'b', "bad");

    assertEquals(ImmutableSet.of(cell1), cellSet);
    assertNotEquals(ImmutableSet.of(cell2), cellSet);

    //two
    ImmutableSetMultitable<Integer, Character, String> myTable2 =
        ImmutableSetMultitable.<Integer, Character, String>builder()
            .put(3, 'a', "all")
            .put(3, 'b', "bad")
            .put(4, 'b', "ball")
            .put(4, 'b', "bass")
            .put(4, 'b', "bass")
            .build();

    ImmutableSetMultitable.SetMulticell<Integer, Character, String> cell3A =
        createTestCell(3, 'a', "all");
    ImmutableSetMultitable.SetMulticell<Integer, Character, String> cell3B =
        createTestCell(3, 'b', "bad");
    ImmutableSetMultitable.SetMulticell<Integer, Character, String> cell4B =
        createTestCell(4, 'b', ImmutableList.of("ball", "bass", "bass"));

    HashSet<ImmutableSetMultitable.SetMulticell<Integer, Character, String>> expected =
        new HashSet<>();
    expected.addAll(ImmutableList.of(cell3A, cell3B, cell4B));

    assertEquals(expected, myTable2.cellSet());
  }

  //helper methods
  private static ImmutableSetMultitable<Integer, Character, String> createTestTable() {
    ImmutableSetMultitable.Builder<Integer, Character, String> builder =
        ImmutableSetMultitable.builder();
    return builder.put(3, 'a', "all")
        .put(3, 'a', "ale")
        .put(3, 'b', "bar")
        .put(3, 'c', "bar")
        .put(4, 'b', "bass")
        .put(4, 'b', "bass")
        .put(5, 'c', "crawl")
        .build();
  }

  private static <R, C, V> ImmutableSetMultitable.SetMulticell<R, C, V>
  createTestCell(R row, C column, Iterable<V> valueCollection) {
    final ImmutableSetMulticell.Builder<R, C, V> builder =
        new ImmutableSetMultitable.SetMulticell.Builder<>();
    return builder.rowKey(row)
      .columnKey(column)
      .addAllValues(valueCollection)
      .build();
  }

  private static <R, C, V> ImmutableSetMultitable.SetMulticell<R, C, V>
  createTestCell(R row, C column, V value) {
    final ImmutableSetMulticell.Builder<R, C, V> builder =
        new ImmutableSetMultitable.SetMulticell.Builder<>();
    builder.rowKey(row);
    builder.columnKey(column);
    builder.addValues(value);
    return builder.build();
  }
}
