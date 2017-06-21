package com.bbn.bue.common.collections;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
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
 * Tests methods in {@link ImmutableListMultitable}
 */
public class ImmutableListMultitableTest {

  @Test
  public void testContainsImmutableList() {
    ImmutableListMultitable<Integer, Character, String> table =
        ImmutableListMultitable.<Integer, Character, String>builder()
            .put(3, 'a', "foo")
            .build();

    final Collection<String> list = table.get(3, 'a');
    assertEquals(ImmutableList.of("foo"), list);
    assertTrue(list instanceof ImmutableList);
  }

  @Test
  public void testEquality() {
    // test insertion order and duplicates don't matter for equality
    ImmutableListMultitable<Integer, Character, String> myTable1 =
        ImmutableListMultitable.<Integer, Character, String>builder()
            .put(3, 'a', "all")
            .put(3, 'b', "bad")
            .put(4, 'b', "bass")
            .put(4, 'b', "bass")
            .build();

    ImmutableListMultitable<Integer, Character, String> myTable2 =
        ImmutableListMultitable.<Integer, Character, String>builder()
            .put(4, 'b', "bass")
            .put(3, 'b', "bad")
            .put(3, 'a', "all")
            .build();
    //duplicates mismatch
    assertNotEquals(myTable1, myTable2);

    ImmutableListMultitable<Integer, Character, String> myTable3 =
        ImmutableListMultitable.<Integer, Character, String>builder()
            .put(4, 'b', "bass")
            .put(4, 'b', "bass")
            .put(3, 'b', "bad")
            .put(3, 'a', "all")
            .build();
    //test insertion order doesn't matter
    assertEquals(myTable1, myTable3);

    //test empty multitables are equal
    ImmutableMultitable<Integer, Character, String> mySetTable =
        ImmutableSetMultitable.<Integer, Character, String>builder().build();
    ImmutableMultitable<Integer, Character, String> myListTable =
        ImmutableListMultitable.<Integer, Character, String>builder().build();
    assertEquals(mySetTable, myListTable);
  }

  @Test
  public void testDuplicates() {
    //check that list maintains duplicate values
    ImmutableListMultitable<Integer, Character, String> myTable =
        ImmutableListMultitable.<Integer, Character, String>builder()
            .put(3, 'a', "all")
            .put(3, 'a', "all")
            .put(3, 'b', "bad")
            .put(4, 'b', "bass")
            .put(4, 'b', "bass")
            .build();
    assertEquals(5, myTable.size());
  }

  @Test
  public void getAsList() {
    //create table and lists to check against
    ImmutableListMultitable<Integer, Character, String> myTable = createTestTable();
    ImmutableList<String> listA = ImmutableList.of("all", "ale");
    ImmutableList<String> listB = ImmutableList.of("bar");
    ImmutableList<String> listDuplicates = ImmutableList.of("bass", "bass");
    ImmutableList<String> listC = ImmutableList.of("crawl");
    ImmutableList<String> emptyList = ImmutableList.of();

    //check that gets the expected lists
    assertEquals(listA, myTable.getAsList(3, 'a'));
    assertEquals(listB, myTable.getAsList(3, 'b'));
    assertEquals(listB, myTable.getAsList(3, 'c'));
    assertEquals(listDuplicates, myTable.getAsList(4, 'b'));
    assertEquals(listC, myTable.getAsList(5, 'c'));

    //check empty cells
    assertEquals(emptyList, myTable.getAsList(4, 'a'));
    assertEquals(emptyList, myTable.getAsList(4, 'c'));
    assertEquals(emptyList, myTable.getAsList(5, 'a'));
    assertEquals(emptyList, myTable.getAsList(5, 'b'));
    assertEquals(emptyList, myTable.getAsList(6, 'a'));
    assertEquals(emptyList, myTable.getAsList(6, 'b'));
    assertEquals(emptyList, myTable.getAsList(6, 'c'));

    //check keys that aren't present
    assertEquals(myTable.getAsList(3, 'n'), emptyList);
    assertEquals(myTable.getAsList(17, 'Z'), emptyList);

    //check empty table
    ImmutableListMultitable<Integer, Character, String> multitable2 =
        ImmutableListMultitable.<Integer, Character, String>builder().build();
    assertEquals(multitable2.getAsList(3, 'a'), emptyList);
  }

  @Test
  public void rowMap() {
    ImmutableMultitable<Integer, Character, String> myTable = createTestTable();
    final Map<Integer, Multimap<Character, String>> myRowMap = myTable.rowMap();

    //test rowMap.get(3)
    final Multimap<Character, String> myMultimap3 = myRowMap.get(3);
    assertEquals(ImmutableList.of("all", "ale"), myMultimap3.get('a'));
    assertEquals(ImmutableList.of("bar"), myMultimap3.get('b'));
    assertEquals(ImmutableList.of("bar"), myMultimap3.get('c'));

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
    assertEquals(ImmutableList.of("bass", "bass"), myMultimap4.get('b'));

    //test RowMap.get(5)
    final Multimap<Character, String> myMultimap5 = myRowMap.get(5);
    assertFalse(myMultimap5.containsKey('a'));
    assertFalse(myMultimap5.containsKey('b'));
    assertEquals(ImmutableList.of("crawl"), myMultimap5.get('c'));
    assertNull(myRowMap.get(6));

    //test empty table
    final ImmutableListMultitable<Integer, Character, String> emptyTable =
        ImmutableListMultitable.<Integer, Character, String>builder().build();
    assertTrue(emptyTable.rowMap().isEmpty());
    assertNull(emptyTable.rowMap().get(1));
  }

  @Test
  public void columnMap() {
    ImmutableMultitable<Integer, Character, String> myTable = createTestTable();
    final Map<Character, Multimap<Integer, String>> myColumnMap = myTable.columnMap();

    //test columnMap.get('a')
    final Multimap<Integer, String> myMultimapA = myColumnMap.get('a');
    assertEquals(ImmutableList.of("all", "ale"), myMultimapA.get(3));
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
    assertEquals(ImmutableList.of("bar"), myMultimap4.get(3));
    assertEquals(ImmutableList.of("bass", "bass"), myMultimap4.get(4));

    //test RowMap.get('c')
    final Multimap<Integer, String> myMultimap5 = myColumnMap.get('c');
    assertFalse(myMultimap5.containsKey(4));
    assertFalse(myMultimap5.containsKey(6));
    assertEquals(ImmutableList.of("crawl"), myMultimap5.get(5));
    assertNull(myColumnMap.get('d'));

    //test empty table
    final ImmutableListMultitable<Integer, Character, String> emptyTable =
        ImmutableListMultitable.<Integer, Character, String>builder().build();
    assertTrue(emptyTable.columnMap().isEmpty());
    assertNull(emptyTable.columnMap().get('a'));
  }

  @Test
  public void values() {
    //empty case
    ImmutableListMultitable<Integer, Character, String> myTable0 =
        ImmutableListMultitable.<Integer, Character, String>builder().build();
    assertTrue(myTable0.values().isEmpty());

    //single case
    ImmutableListMultitable<Integer, Character, String> myTable1 =
        ImmutableListMultitable.<Integer, Character, String>builder()
            .put(3, 'a', "all")
            .build();
    assertEquals(1, myTable1.values().size());
    assertTrue(myTable1.values().contains("all"));

    //two
    ImmutableListMultitable<Integer, Character, String> myTable2 =
        ImmutableListMultitable.<Integer, Character, String>builder()
            .put(3, 'a', "all")
            .put(4, 'b', "bat")
            .put(4, 'b', "bat")
            .put(5, 'b', "bat")
            .build();
    //check that for same row-col eliminates duplicates, but not for different cell
    assertEquals(4, myTable2.values().size());
    assertEquals(ImmutableList.of("all", "bat", "bat", "bat"), myTable2.values());
    assertNotEquals(ImmutableList.of("all", "bat", "bat"), myTable2.values());
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
    ImmutableListMultitable<Integer, Character, String> myTable0 =
        ImmutableListMultitable.<Integer, Character, String>builder().build();
    assertFalse(myTable0.containsRow(3));
    assertFalse(myTable0.containsRow('b'));

    //single case
    ImmutableListMultitable<Integer, Character, String> myTable1 =
        ImmutableListMultitable.<Integer, Character, String>builder()
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
    ImmutableListMultitable<Integer, Character, String> myTable0 =
        ImmutableListMultitable.<Integer, Character, String>builder().build();
    assertFalse(myTable0.containsColumn(3));
    assertFalse(myTable0.containsColumn('c'));

    //single case
    ImmutableListMultitable<Integer, Character, String> myTable1 =
        ImmutableListMultitable.<Integer, Character, String>builder()
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
    ImmutableListMultitable<Integer, Character, String> myTable0 =
        ImmutableListMultitable.<Integer, Character, String>builder().build();
    assertFalse(myTable0.containsValue("all"));
    assertFalse(myTable0.containsValue("bar"));
    //one
    ImmutableListMultitable<Integer, Character, String> myTable1 =
        ImmutableListMultitable.<Integer, Character, String>builder()
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
    ImmutableListMultitable<Integer, Character, String> myTable0 =
        ImmutableListMultitable.<Integer, Character, String>builder().build();
    assertTrue(myTable0.isEmpty());
    //single case
    ImmutableListMultitable<Integer, Character, String> myTable1 =
        ImmutableListMultitable.<Integer, Character, String>builder()
            .put(3, 'a', "all")
            .build();
    assertFalse(myTable1.isEmpty());
    //many
    ImmutableListMultitable<Integer, Character, String> myTable2 =
        ImmutableListMultitable.<Integer, Character, String>builder()
            .putAll(3, 'a', ImmutableList.of("all", "ale"))
            .putAll(4, 'b', ImmutableList.of("bass", "bass"))
            .build();
    assertFalse(myTable2.isEmpty());
  }

  @Test
  public void size() {
    //empty case
    ImmutableListMultitable<Integer, Character, String> myTable0 =
        ImmutableListMultitable.<Integer, Character, String>builder().build();
    assertEquals(0, myTable0.size());

    //single case
    ImmutableListMultitable<Integer, Character, String> myTable1 =
        ImmutableListMultitable.<Integer, Character, String>builder()
            .put(3, 'a', "all")
            .build();
    assertEquals(1, myTable1.size());

    //single case w/duplicate
    ImmutableListMultitable<Integer, Character, String> myTable1Duplicate =
        ImmutableListMultitable.<Integer, Character, String>builder()
            .put(3, 'a', "all")
            .put(3, 'a', "all")
            .build();
    assertEquals(2, myTable1Duplicate.size());

    //two
    ImmutableListMultitable<Integer, Character, String> myTable2 =
        ImmutableListMultitable.<Integer, Character, String>builder()
            .put(3, 'a', "all")
            .put(4, 'b', "bat")
            .build();
    assertEquals(2, myTable2.size());

    //many, tests duplicates in table
    ImmutableMultitable<Integer, Character, String> myTable3 = createTestTable();
    assertEquals(7, myTable3.size()); //larger because list backing counts duplicates
    assertNotEquals(6, myTable3.size());
  }

  @Test
  public void putAll() {
    //check putAll for both list and set collections
    ImmutableList<String> setA = ImmutableList.of("all", "ale", "bat");
    ImmutableList<String> listB = ImmutableList.of("bat", "bat");
    ImmutableListMultitable<Integer, Character, String> myTable3 =
        ImmutableListMultitable.<Integer, Character, String>builder()
            .putAll(3, 'a', setA)
            .putAll(4, 'b', listB)
            .build();
    //double check size again to ensure duplicates in a cell are not counted.
    assertEquals(5, myTable3.size());
    assertNotEquals(4, myTable3.size());
  }

  @Test
  public void rowKeySet() {
    //empty case
    ImmutableListMultitable<Integer, Character, String> myTable0 =
        ImmutableListMultitable.<Integer, Character, String>builder().build();
    assertTrue(myTable0.rowKeySet().isEmpty());

    //single case
    ImmutableListMultitable<Integer, Character, String> myTable1 =
        ImmutableListMultitable.<Integer, Character, String>builder()
            .put(3, 'a', "all")
            .build();
    assertEquals(1, myTable1.rowKeySet().size());
    assertTrue(myTable1.rowKeySet().contains(3));

    //two
    ImmutableListMultitable<Integer, Character, String> myTable2 =
        ImmutableListMultitable.<Integer, Character, String>builder()
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
    ImmutableListMultitable<Integer, Character, String> myTable0 =
        ImmutableListMultitable.<Integer, Character, String>builder().build();
    assertTrue(myTable0.columnKeySet().isEmpty());

    //single case
    ImmutableListMultitable<Integer, Character, String> myTable1 =
        ImmutableListMultitable.<Integer, Character, String>builder()
            .put(3, 'a', "all")
            .build();
    assertEquals(1, myTable1.rowKeySet().size());
    assertTrue(myTable1.columnKeySet().contains('a'));

    //two
    ImmutableListMultitable<Integer, Character, String> myTable2 =
        ImmutableListMultitable.<Integer, Character, String>builder()
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
    ImmutableListMultitable<Integer, Character, String> myTable0 =
        ImmutableListMultitable.<Integer, Character, String>builder().build();
    assertTrue(myTable0.get(3, 'a').isEmpty());

    //single case
    ImmutableListMultitable<Integer, Character, String> myTable1 =
        ImmutableListMultitable.<Integer, Character, String>builder()
            .put(3, 'a', "all")
            .build();
    assertEquals(ImmutableList.of("all"), myTable1.get(3, 'a'));

    //two
    ImmutableListMultitable<Integer, Character, String> myTable2 =
        ImmutableListMultitable.<Integer, Character, String>builder()
            .put(3, 'a', "all")
            .put(3, 'a', "ale")
            .put(4, 'b', "bat")
            .put(4, 'b', "bat")
            .build();
    assertEquals(ImmutableList.of("all", "ale"), myTable2.get(3, 'a'));
    assertEquals(ImmutableList.of("bat", "bat"), myTable2.get(4, 'b'));
    assertTrue(myTable2.get(3, 'b').isEmpty());
    assertTrue(myTable2.get(1000, 'd').isEmpty());
  }

  @Test
  public void row() {
    //empty case
    ImmutableListMultitable<Integer, Character, String> myTable0 =
        ImmutableListMultitable.<Integer, Character, String>builder().build();
    assertTrue(myTable0.row(1).isEmpty());

    //check immutability (guaranteed because returned maps are immutable)
    try {
      Multimap<Character, String> myMap = myTable0.row(1);
      myMap.putAll('a', ImmutableList.of("apple", "add"));
      fail();
    } catch(UnsupportedOperationException expected) {
      //correct behavior
    }
    //single case
    ImmutableListMultitable<Integer, Character, String> myTable1 =
        ImmutableListMultitable.<Integer, Character, String>builder()
            .put(3, 'a', "all")
            .build();
    assertEquals(ImmutableListMultimap.of('a', "all"), myTable1.row(3));
    assertTrue(myTable1.row(4).isEmpty());
    assertTrue(myTable1.row(3).get('a').contains("all"));

    //two
    ImmutableListMultitable<Integer, Character, String> myTable2 =
        ImmutableListMultitable.<Integer, Character, String>builder()
            .put(3, 'a', "all")
            .put(3, 'b', "bad")
            .put(4, 'b', "bass")
            .put(4, 'b', "bass")
            .build();
    //check rows
    assertEquals(ImmutableListMultimap.of('a', "all", 'b', "bad"),
        myTable2.row(3));
    assertEquals(ImmutableListMultimap.of('b', "bass",'b', "bass"), myTable2.row(4));
    //check empty
    assertTrue(myTable2.row(6).isEmpty());
  }

  @Test
  public void column() {
    //empty case
    ImmutableListMultitable<Integer, Character, String> myTable0 =
        ImmutableListMultitable.<Integer, Character, String>builder().build();
    assertTrue(myTable0.column('a').isEmpty());
    //check immutability (guaranteed because returned maps are immutable)
    try {
      Multimap<Integer, String> myMap = myTable0.column('a');
      myMap.putAll(1, ImmutableList.of("apple", "add"));
      fail();
    } catch(UnsupportedOperationException expected) {
      //correct behavior
    }
    //single case
    ImmutableListMultitable<Integer, Character, String> myTable1 =
        ImmutableListMultitable.<Integer, Character, String>builder()
            .put(3, 'a', "all")
            .build();
    assertEquals(ImmutableListMultimap.of(3, "all"), myTable1.column('a'));
    assertTrue(myTable1.column('b').isEmpty());

    //two
    ImmutableListMultitable<Integer, Character, String> myTable2 =
        ImmutableListMultitable.<Integer, Character, String>builder()
            .put(3, 'a', "all")
            .put(3, 'b', "bad")
            .put(4, 'b', "bass")
            .put(4, 'b', "bass")
            .build();
    //check columns
    assertEquals(ImmutableListMultimap.of(3, "all"),
        myTable2.column('a'));
    assertEquals(ImmutableListMultimap.of(3, "bad", 4, "bass", 4, "bass" ),
        myTable2.column('b'));
    //check empty
    assertTrue(myTable2.column('c').isEmpty());
  }

  @Test
  public void cellSet() {
    //empty case
    ImmutableListMultitable<Integer, Character, String> myTable0 =
        ImmutableListMultitable.<Integer, Character, String>builder().build();
    assertTrue(myTable0.cellSet().isEmpty());

    //single case
    ImmutableListMultitable<Integer, Character, String> myTable1 =
        ImmutableListMultitable.<Integer, Character, String>builder()
            .put(3, 'a', "all")
            .build();
    Set<Multitable.Multicell<Integer, Character, String>> cellSet = myTable1.cellSet();

    ImmutableListMultitable.ListMulticell<Integer, Character, String> cell1 =
        createTestCell(3, 'a', "all");
    ImmutableListMultitable.ListMulticell<Integer, Character, String> cell2 =
        createTestCell(4, 'b', "bad");

    assertEquals(ImmutableSet.of(cell1), cellSet);
    assertNotEquals(ImmutableSet.of(cell2), cellSet);
    //two
    ImmutableListMultitable<Integer, Character, String> myTable2 =
        ImmutableListMultitable.<Integer, Character, String>builder()
            .put(3, 'a', "all")
            .put(3, 'b', "bad")
            .put(4, 'b', "ball")
            .put(4, 'b', "bass")
            .put(4, 'b', "bass")
            .build();

    ImmutableListMultitable.ListMulticell<Integer, Character, String> cell3A =
        createTestCell(3, 'a', "all");
    ImmutableListMultitable.ListMulticell<Integer, Character, String> cell3B =
        createTestCell(3, 'b', "bad");
    ImmutableListMultitable.ListMulticell<Integer, Character, String> cell4B =
        createTestCell(4, 'b', ImmutableList.of("ball", "bass", "bass"));

    final HashSet<ImmutableListMultitable.ListMulticell<Integer, Character, String>> expected =
        new HashSet<>();
    expected.addAll(ImmutableList.of(cell3A, cell3B, cell4B));

    assertEquals(expected, myTable2.cellSet());
  }

  //helper methods
  private static ImmutableListMultitable<Integer, Character, String> createTestTable() {
    ImmutableListMultitable.Builder<Integer, Character, String> builder =
        ImmutableListMultitable.builder();
    return builder.put(3, 'a', "all")
        .put(3, 'a', "ale")
        .put(3, 'b', "bar")
        .put(3, 'c', "bar")
        .put(4, 'b', "bass")
        .put(4, 'b', "bass")
        .put(5, 'c', "crawl")
        .build();
  }

  private static <R, C, V> ImmutableListMultitable.ListMulticell<R, C, V>
  createTestCell(R row, C column, Iterable<V> valueCollection) {
    final ImmutableListMulticell.Builder<R, C, V> builder =
        new ImmutableListMultitable.ListMulticell.Builder<>();
    return builder.rowKey(row)
        .columnKey(column)
        .addAllValues(valueCollection)
        .build();
  }

  private static <R, C, V> ImmutableListMultitable.ListMulticell<R, C, V>
  createTestCell(R row, C column, V value) {
    final ImmutableListMulticell.Builder<R, C, V> builder =
        new ImmutableListMultitable.ListMulticell.Builder<>();
    builder.rowKey(row);
    builder.columnKey(column);
    builder.addValues(value);
    return builder.build();
  }
}
