package dictarray

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

/*******************************************************************************
 * Abstract class with methods and properties used by both Array and Dictionary
 * classes.
 *
 * @author Charles Ross <chivalry@mac.com>
 * @version 1.0
 ******************************************************************************/
class Collect {
  /**---------------------------------------------------------------------------
   * Stores the Groovy Map or List that is translated whenever needed.
   */
  def object

  /**---------------------------------------------------------------------------
   * A builder is used often enough that we simply keep an instance variable.
   */
  JsonBuilder builder

  /**---------------------------------------------------------------------------
   * A parser is used often enough that we simply keep an instance variable.
   */
  JsonSlurper parser
  
  /**---------------------------------------------------------------------------
   * All initiators should call this. It should operate as the default
   * initiator, but I'm unsure how to impliment that in Groovy.
   */
  def init() {
    builder = new JsonBuilder()
    parser  = new JsonSlurper()
  }
  
  /**---------------------------------------------------------------------------
   * The initiator for when a dictionary or array is created with no parameters.
   */
  def Collect() {
    this.init()
  }

  /**---------------------------------------------------------------------------
   * Initiate the Collect object with a string that was received as output from
   * another Collect object.
   */
  def Collect( String input ) {
    this.init()
    this.object = this.parser.parseText( input )
  }
  
  /**---------------------------------------------------------------------------
   * Assume that if a string value is passed and it looks like something else
   * (number, array or dictionary), it should be treated as that instead of a
   * string.
   */
  Object convertValue( Object value ) {
    if ( value instanceof String ) {
      if ( value.isInteger() ) {
        value = value.toInteger()
      } else if ( value.isDouble() ) {
        value = value.toDouble()
      } else if ( value.startsWith( ['['] )
                | value.startsWith( ['{'] ) ) {
        value = this.parser.parseText( value )
      }
    }
    
    return value
  }
  
  /**---------------------------------------------------------------------------
   * Convert the object (a List or a Map) into a JSON representation.
   */
  String getOutput() {
    this.builder.content = this.object
    return this.builder.toPrettyString()
  }
  
  /**---------------------------------------------------------------------------
   * Return the number of items in the object. Works for both Maps and Lists.
   */
  Integer count() {
    return this.object.size()
  }
}

/*******************************************************************************
 * Internally uses a List to store data, but all external output is via JSON.
 *
 * @author Charles Ross <chivalry@mac.com>
 * @version 1.0
 ******************************************************************************/
class Array extends Collect {
  
  /**---------------------------------------------------------------------------
   * Empty Arrays start out as empty Lists.
   */
  def Array() {
    super.init()
    this.object = []
  }
  
  /**---------------------------------------------------------------------------
   * Initialize an Array with a List, converting any values as needed.
   */
  def Array( List list ) {
    super.init()
    List newList = []
    list.each { entry -> newList.add( convertValue( entry ) ) }
    this.object = newList
  }
  
  /**---------------------------------------------------------------------------
   * Initialize an Array with the output from another array.
   */
  def Array( String input ) {
    super( input )
  }
  
  /**---------------------------------------------------------------------------
   * Add the specified value to the end of the Array.
   */
  void add( Object value ) {
    this.object.add( this.convertValue( value ) )
  }
  
  /**---------------------------------------------------------------------------
   * Return the first value in the list.
   */
  Object head() {
    return this.object.head()
  }
  
  /**---------------------------------------------------------------------------
   * Return a new list with everything except the first value.
   */
  Object tail() {
    return new Array( this.object.tail() )
  }
  
  /**---------------------------------------------------------------------------
   * Return the value at the specified index.
   */
  Object value( Integer index ) {
    return this.object[ index ]
  }
  
  /**---------------------------------------------------------------------------
   * Return true if there are no values in the Array.
   */
  Boolean isEmpty() {
    return this.count() == 0
  }
  
  /**---------------------------------------------------------------------------
   * Overloading of the the [] operator for retrieval.
   */
  Object getAt( Integer index ) {
    return this.value( index )
  }
  
  /**---------------------------------------------------------------------------
   * Overloading of the the [] operator for assignment.
   */
  void putAt( Integer index, Object value ) {
    this.object[ index ] = this.convertValue( value )
  }
  
  /**---------------------------------------------------------------------------
   * Overloading of the the == operator for comparison.
   */
  Boolean equals( Array array ) {
    return this.object == array.object
  }
}

/*******************************************************************************
 * Internally uses a List to store data, but all external output is via JSON.
 *
 * @author Charles Ross <chivalry@mac.com>
 * @version 1.0
 ******************************************************************************/
class Dictionary extends Collect {
  
  /**---------------------------------------------------------------------------
   * Empty Dictionaries start out as empty Maps.
   */
  def Dictionary() {
    super.init()
    this.object = [:]
  }
  
  /**---------------------------------------------------------------------------
   * Initialize an Dictionary with a the output from another dictionary.
   */
  def Dictionary( String input ) {
    super( input )
  }
  
  /**---------------------------------------------------------------------------
   * Initialize an Dictionary with a Map, converting any values as needed.
   */
  def Dictionary( Map map ) {
    super.init()
    Map newMap = [:]
    map.each{ key, value -> newMap[key] = convertValue( value ) }
    this.object = newMap
  }
  
  /**---------------------------------------------------------------------------
   * Set the value for the specified key, overriding any existing value if it
   * exists.
   */
  def setValueForKey( String key, Object value ) {
    this.object[ key ] = this.convertValue( value )
  }
  
  /**---------------------------------------------------------------------------
   * Return the value stored for the specified key.
   */
  def getValueForKey( String key ) {
    def value = this.object[ key ]
    return this.convertValue( value )
  }
  
  /**---------------------------------------------------------------------------
   * Return true if there are no key/value pairs in the Dictionary.
   */
  def isEmpty() {
    return this.count() == 0
  }
  
  /**---------------------------------------------------------------------------
   * Overloading of the the [] operator for retrieval.
   */
  def getAt( String key ) {
    return this.getValueForKey( key )
  }
  
  /**---------------------------------------------------------------------------
   * Overloading of the the [] operator for assignment.
   */
  def setAt( String key, Object value ) {
    this.setValueForKey( key, value )
  }
  
  /**---------------------------------------------------------------------------
   * Overloading of the the == operator for comparison.
   */
  Boolean equals( Dictionary dict ) {
    return this.object == dict.object
  }
}

/*******************************************************************************
 * A collection of static methods that can be directly called with parameters
 * supplied by FileMaker.
 *
 * @author Charles Ross <chivalry@mac.com>
 * @version 1.0
 ******************************************************************************/
class FM {
  
  /**---------------------------------------------------------------------------
   * If the array parameter is empty, create a new empty array, otherwise use
   * the string passed
   */
  static Array createArray( array ) {
    if ( array == '' ) {
      return new Array()
    } else {
      return new Array( array )
    }
  }
  
  /**---------------------------------------------------------------------------
   * If the dict parameter is empty, create a new empty dictionary, otherwise
   * use the string passed
   */
  static Dictionary createDict( dict ) {
    if ( dict == '' ) {
      return new Dictionary()
    } else {
      return new Dictionary( dict )
    }
  }
  
  /**---------------------------------------------------------------------------
   * If the parameter provided is a list or a map, convert to the JSON
   * representation.
   */
  static String convertListOrMap( list_or_map ) {
    if ( ( list_or_map instanceof List )
       | ( list_or_map instanceof Map ) ) {
      list_or_map = new JsonBuilder( list_or_map ).toPrettyString()
    }
    return list_or_map
  }
  
  /**---------------------------------------------------------------------------
   * An exact analog to the anticipated FileMaker custom function arry_Add.
   */
  static String arry_Add( array, value ) {
    Array newArray = this.createArray( array )
    newArray.add( value )
    return newArray.output
  }
  
  /**---------------------------------------------------------------------------
   * An exact analog to the anticipated FileMaker custom function arry_Count.
   */
  static Integer arry_Count( array ) {
    Array newArray = this.createArray( array )
    return newArray.count()
  }
  
  /**---------------------------------------------------------------------------
   * An exact analog to the anticipated FileMaker custom function arry_Head.
   */
  static String arry_Head( array ) {
    Array newArray = this.createArray( array )
    return newArray.head()
  }
  
  /**---------------------------------------------------------------------------
   * An exact analog to the anticipated FileMaker custom function arry_IsEmpty.
   */
  static Boolean arry_IsEmpty( array ) {
    Array newArray = this.createArray( array )
    return newArray.isEmpty()
  }
  
  /**---------------------------------------------------------------------------
   * An exact analog to the anticipated FileMaker custom function arry_Tail.
   */
  static String arry_Tail( array ) {
    Array newArray = this.createArray( array )
    return newArray.tail().output
  }
  
  /**---------------------------------------------------------------------------
   * An exact analog to the anticipated FileMaker custom function arry_Value.
   */
  static String arry_Value( array, index ) {
    Array newArray = this.createArray( array )
    def value = newArray.value( index.toInteger() )
    return this.convertListOrMap( value )
  }
  
  /**---------------------------------------------------------------------------
   * An exact analog to the anticipated FileMaker custom function dict_Count.
   */
  static Integer dict_Count( dict ) {
    Dictionary newDict = this.createDict( dict )
    return newDict.count()
  }
  
  /**---------------------------------------------------------------------------
   * An exact analog to the anticipated FileMaker custom function
   * dict_GetValueForKey.
   */
  static String dict_GetValueForKey( dict, key ) {
    Dictionary newDict = this.createDict( dict )
    def value = newDict.getValueForKey( key )
    return this.convertListOrMap( value )
  }
  
  /**---------------------------------------------------------------------------
   * An exact analog to the anticipated FileMaker custom function dict_IsEmpty.
   */
  static Boolean dict_IsEmpty( dict ) {
    Dictionary newDict = this.createDict( dict )
    return newDict.isEmpty()
  }
  
  /**---------------------------------------------------------------------------
   * An exact analog to the anticipated FileMaker custom function
   * dict_RemoveKey.
   */
  static String dict_RemoveKey( dict, key ) {
    Dictionary newDict = this.createDict( dict )
    return newDict.remove( key ).output
  }
  
  /**---------------------------------------------------------------------------
   * An exact analog to the anticipated FileMaker custom function
   * dict_SetValueForKey.
   */
  static String dict_SetValueForKey( dict, key, value ) {
    Dictionary newDict = this.createDict( dict )
    newDict.setValueForKey( key, value )
    return newDict.output
  }
}