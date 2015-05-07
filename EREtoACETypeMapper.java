package com.bbn.nlp.corpora.ere;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.bbn.bue.common.parameters.Parameters;
import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;
import com.google.common.io.Resources;

public final class EREtoACETypeMapper {
  private final ImmutableMap<String, String> eventTypes;
  private final ImmutableMap<String, String> eventSubtypes;
  private final ImmutableTable<String, String, String> eventRoles;
  private final ImmutableMap<String, String> fillerTypes;
  private final ImmutableMap<String, String> fillerSubtypes;
  
  
  private EREtoACETypeMapper(final Map<String, String> eventTypes, final Map<String, String> eventSubtypes, 
      final Table<String, String, String> eventRoles,
      final Map<String, String> fillerTypes, final Map<String, String> fillerSubtypes) {
    this.eventTypes = ImmutableMap.copyOf(eventTypes);
    this.eventSubtypes = ImmutableMap.copyOf(eventSubtypes);
    this.eventRoles = ImmutableTable.copyOf(eventRoles);
    this.fillerTypes = ImmutableMap.copyOf(fillerTypes);
    this.fillerSubtypes = ImmutableMap.copyOf(fillerSubtypes);
  }
  
  
  
  public static EREtoACETypeMapper from(final Parameters params) throws IOException {
    //ClassLoader classLoader = EREtoACETypeMapper.class.getClassLoader();
    //File file = new File(classLoader.getResource(params.getString("ereToAce.event.mappings")).getFile());
    //System.out.println(file.getAbsolutePath());
    //System.out.println(file.getPath());
    
    final ImmutableList<String> eventLines = linesFromResourceFile(params.getString("ereToAce.event.mappings"));    
    //final CharSource eventsIn = Files.asCharSource(new File(EREtoACETypeMapper.class.getClassLoader().getResource(params.getString("ereToAce.event.mappings")).getFile()), Charsets.UTF_8);
    //final ImmutableList<String> eventLines = eventsIn.readLines();
    
    final ImmutableMap<String, String> eTypes = mapEventTypes(eventLines);
    final ImmutableMap<String, String> eSubtypes = mapEventSubtypes(eventLines);
    final ImmutableTable<String, String, String> eRoles = mapEventRoles(eventLines);
    
    final ImmutableList<String> fillerLines = linesFromResourceFile(params.getString("ereToAce.filler.mappings"));
    //final CharSource fillersIn = Files.asCharSource(new File(EREtoACETypeMapper.class.getResource("/"+ params.getString("ereToAce.filler.mappings")).getFile()), Charsets.UTF_8);
    //final ImmutableList<String> fillerLines = fillersIn.readLines();
    
    final ImmutableMap<String, String> fTypes = mapFillerTypes(fillerLines);
    final ImmutableMap<String, String> fSubtypes = mapFillerSubtypes(fillerLines);
    
    return new EREtoACETypeMapper(eTypes, eSubtypes, eRoles, fTypes, fSubtypes);
  }
  
  public String mapEventType(final String fromEventType) {
    return eventTypes.get(fromEventType);
  }
  
  public String mapEventSubtype(final String fromEventSubtype) {
    return eventSubtypes.get(fromEventSubtype);
  }
  
  public String mapEventRole(final String fromEventType, final String fromEventSubtype, final String fromEventRole) {
    return eventRoles.get(fromEventType+"."+fromEventSubtype, fromEventRole);
  }
  
  public String mapFillerType(final String fromType) {
    return fillerTypes.get(fromType);
  }
  
  public Optional<String> mapFillerSubtype(final String fromType) {
    return Optional.fromNullable(fillerSubtypes.get(fromType));
  }
  
  
  // ============== Events START ==============
  private static ImmutableMap<String, String> mapEventTypes(final ImmutableList<String> lines) {
    final ImmutableMap.Builder<String, String> ret = ImmutableMap.builder();
    
    Set<String> keys = Sets.newHashSet();
    for(final String line : lines) {
      final String[] tokens = line.split(" ");
      final String[] fromEvent = tokens[0].split(Pattern.quote("."));  // type.subtype
      final String[] toEvent = tokens[2].split(Pattern.quote("."));    // type.subtype
      
      if(!keys.contains(fromEvent[0])) {
        ret.put(fromEvent[0], toEvent[0]);
        keys.add(fromEvent[0]);
      }
    }
    
    return ret.build();
  }
  
  private static ImmutableMap<String, String> mapEventSubtypes(final ImmutableList<String> lines) {
    final ImmutableMap.Builder<String, String> ret = ImmutableMap.builder();
    
    Set<String> keys = Sets.newHashSet();
    for(final String line : lines) {
      final String[] tokens = line.split(" ");
      final String[] fromEvent = tokens[0].split(Pattern.quote("."));  // type.subtype
      final String[] toEvent = tokens[2].split(Pattern.quote("."));    // type.subtype
      
      if(!keys.contains(fromEvent[1])) {
        ret.put(fromEvent[1], toEvent[1]);
        keys.add(fromEvent[1]);
      }
    }
    
    return ret.build();
  }
  
  private static ImmutableTable<String, String, String> mapEventRoles(final ImmutableList<String> lines) {
    final ImmutableTable.Builder<String, String, String> ret = ImmutableTable.builder();

    for(final String line : lines) {
      final String[] tokens = line.split(" ");
      
      final String fromEvent = tokens[0];
      final String fromRole = tokens[1];
      final String toEvent = tokens[2];
      final String toRole = tokens[3];
      ret.put(fromEvent, fromRole, toRole);
    }
    
    return ret.build();
  }
  // ================== Events END =================
  
  // ================== Fillers START =================
  private static ImmutableMap<String, String> mapFillerTypes(final ImmutableList<String> lines) {
    final ImmutableMap.Builder<String, String> ret = ImmutableMap.builder();
    
    Set<String> keys = Sets.newHashSet();
    for(final String line : lines) {
      final String[] tokens = line.split(" ");
      final String fromFillerType = tokens[0];
      final String toType = tokens[1].split(Pattern.quote("."))[0];
      
      if(!keys.contains(fromFillerType)) {
        ret.put(fromFillerType, toType);
        keys.add(fromFillerType);
      }
    }
    
    return ret.build();
  }
  
  private static ImmutableMap<String, String> mapFillerSubtypes(final ImmutableList<String> lines) {
    final ImmutableMap.Builder<String, String> ret = ImmutableMap.builder();
    
    Set<String> keys = Sets.newHashSet();
    for(final String line : lines) {
      final String[] tokens = line.split(" ");
      final String fromFillerType = tokens[0];
      final String toType = tokens[1].split(Pattern.quote("."))[1];
      
      if(!keys.contains(fromFillerType)) {
        ret.put(fromFillerType, toType);
        keys.add(fromFillerType);
      }
    }
    
    return ret.build();
  }
  // =================== Fillers END =====================
  
  public String toString() {
    StringBuffer s = new StringBuffer("");
    
    s.append("Event Types:\n" + toString(mapToString(eventTypes)) + "\n");
    s.append("Event Subtypes:\n" + toString(mapToString(eventSubtypes)) + "\n");
    s.append("Event Roles:\n" + toString(tableToString(eventRoles)) + "\n");
    s.append("Filler Types:\n" + toString(mapToString(fillerTypes)) + "\n");
    s.append("Filler Subtypes:\n" + toString(mapToString(fillerSubtypes)) + "\n");
    
    return s.toString();
  }
  
  private String toString(final ImmutableList<String> lines) {
    StringBuffer s = new StringBuffer("");
    for(final String line : lines) {
      s.append(line + "\n");
    }
    return s.toString();
  }
  
  private ImmutableList<String> mapToString(final ImmutableMap<String, String> map) {
    final ImmutableList.Builder<String> ret = ImmutableList.builder();
    
    for(final Map.Entry<String, String> entry : map.entrySet()) {
      ret.add(entry.getKey() + " " + entry.getValue());
    }
    
    return ret.build();
  }
  
  private ImmutableList<String> tableToString(final ImmutableTable<String, String, String> table) {
    final ImmutableList.Builder<String> ret = ImmutableList.builder();
    
    for(final Cell<String, String, String> cell : table.cellSet()) {
      ret.add(cell.getRowKey() + " " + cell.getColumnKey() + " : " + cell.getValue());
    }
    
    return ret.build();
  }
  
  private static ImmutableList<String> linesFromResourceFile(final String resourceName) throws IOException {
    return Resources.asCharSource(Resources.getResource(EREtoACETypeMapper.class, resourceName), Charsets.UTF_8).readLines();
    
    /*
    final ImmutableList.Builder<String> ret = ImmutableList.builder();
    
    final InputStream inputStream = Resources.getResource(EREtoACETypeMapper.class, resourceName).openStream();
    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
    
    String line;
    while((line = bufferedReader.readLine())!=null) {
      ret.add(line);
    }
    
    return ret.build();
    */
  }
  
}


