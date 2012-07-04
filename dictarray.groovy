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
   * 
   */
  def object
  JsonBuilder builder
  JsonSlurper parser
  
  def init() {
    builder = new JsonBuilder()
    parser  = new JsonSlurper()
  }
  
  /**---------------------------------------------------------------------------
   * Empty initiator, this doesn't work without it, don't know enough about
   * Groovy objects to know why. When I remove it, I get an error when
   * attempting to create a new Array (subclass of Collect) with a List
   * parameter.
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
  
  Object getAt( Integer index ) {
    return this.value( index )
  }
  
  void putAt( Integer index, Object value ) {
    this.object[ index ] = this.convertValue( value )
  }
  
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
  
  def Dictionary() {
    super.init()
    this.object = [:]
  }
  
  def Dictionary( String input ) {
    super( input )
  }
  
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
  
  def getAt( String key ) {
    return this.getValueForKey( key )
  }
  
  def setAt( String key, Object value ) {
    this.setValueForKey( key, value )
  }
  
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
  
  static Array createArray( array ) {
    if ( array == '' ) {
      return new Array()
    } else {
      return new Array( array )
    }
  }
  
  static Dictionary createDict( dict ) {
    if ( dict == '' ) {
      return new Dictionary()
    } else {
      return new Dictionary( dict )
    }
  }
  
  static String arry_Add( array, value ) {
    Array newArray = this.createArray( array )
    newArray.add( value )
    return newArray.output
  }
  
  static Integer arry_Count( array ) {
    Array newArray = this.createArray( array )
    return newArray.count()
  }
  
  static String arry_Head( array ) {
    Array newArray = this.createArray( array )
    return newArray.head()
  }
  
  static Boolean arry_IsEmpty( array ) {
    Array newArray = this.createArray( array )
    return newArray.isEmpty()
  }
  
  static String arry_Tail( array ) {
    Array newArray = this.createArray( array )
    return newArray.tail().output
  }
  
  static String arry_Value( array, index ) {
    Array newArray = this.createArray( array )
    return newArray.value( index.toInteger() )
  }
  
  static Integer dict_Count( dict ) {
    Dictionary newDict = this.createDict( dict )
    return newDict.count()
  }
  
  static String dict_GetValueForKey( dict, key ) {
    Dictionary newDict = this.createDict( dict )
    def value = newDict.getValueForKey( key )
    if ( ( value instanceof List )
       | ( value instanceof Map ) ) {
      value = new JsonBuilder( value ).toPrettyString()
    }
    return value
  }
  
  static Boolean dict_IsEmpty( dict ) {
    Dictionary newDict = this.createDict( dict )
    return newDict.isEmpty()
  }
  
  static String dict_RemoveKey( dict, key ) {
    Dictionary newDict = this.createDict( dict )
    return newDict.remove( key ).output
  }
  
  static String dict_SetValueForKey( dict, key, value ) {
    Dictionary newDict = this.createDict( dict )
    newDict.setValueForKey( key, value )
    return newDict.output
  }
}