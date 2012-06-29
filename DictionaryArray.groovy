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
  boolean isEmpty() {
    return this.count() == 0
  }
  
  Object getAt( Integer index ) {
    return this.value( index )
  }
  
  void putAt( Integer index, Object value ) {
    this.object[ index ] = this.convertValue( value )
  }
  
  boolean equals( Array array ) {
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
    this.object[ key ] = value
  }
  
  /**---------------------------------------------------------------------------
   * Return the value stored for the specified key.
   */
  def getValueForKey( String key ) {
    return this.object[ key ]
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
  
  boolean equals( Dictionary dict ) {
    return this.object == dict.object
  }
}

/*******************************************************************************
 * Test the Collect class
 ******************************************************************************/

Collect     col
JsonBuilder json
String      output
List        list
Map         map

col = new Collect()
assert col.object == null
col = new Collect( '[1,2,3]' )
assert col.object == [ 1, 2, 3 ]
col = new Collect( '{"a":1,"b":2,"c":3}' )
assert col.object == [ 'a':1, 'b':2, 'c':3 ]

assert col.convertValue( '2' ) == 2
assert col.convertValue( 'a' ) == 'a'
assert col.convertValue( '[1,2]' ) == [ 1, 2 ]
assert col.convertValue( '{"a":1,"b":2,"c":3}' ) == [ 'a':1, 'b':2, 'c':3 ]
assert col.convertValue( 1 ) == 1

map = [ 'string':'a string', 'number':3.14 ]
json = new JsonBuilder( map )
output = json.toPrettyString()
col = new Collect( output )
assert col.output == output

list = ['a','b','c',4,5,6]
json = new JsonBuilder( list )
output = json.toPrettyString()
col = new Collect( output )
assert col.output == output

assert col.count() == 6

/*******************************************************************************
 * Test the Array class
 ******************************************************************************/

Array array

array = new Array()
assert array.isEmpty()
array = new Array( [ 'one', '2', 3.14 ] )
assert array.count() == 3
assert array.value( 0 ) instanceof String
assert array.value( 1 ) instanceof Integer
assert array.value( 2 ) == 3.14

array = new Array( output )
assert array.value( 0 ) == 'a'
assert array.count() == 6

array.add( 'seven' )
assert array.count() == 7
assert array.value( 6 ) == 'seven'

assert array.head() == 'a'
assert array.tail().object == ['b','c',4,5,6,'seven']

assert array[6] == 'seven'
array[6] = 'six'
assert array.value(6) == 'six'
assert array[6] == 'six'

array1 = new Array( list )
array2 = new Array( list )
assert array1 == array2
assert array1.output == array2.output

/*******************************************************************************
 * Test the Dictionary class
 ******************************************************************************/

Dictionary dict
dict = new Dictionary()
assert dict.object == [:]
assert dict.isEmpty()
dict.setValueForKey( "string", "value" )
assert dict.getValueForKey( "string" ) == "value"

map = ['string':'value']
map['number'] = 3.14
inner_dict = ['a':1,'b':2,'c':3 ]
map['dictionary'] = inner_dict
inner_array = [6,7,8]
map['array'] = inner_array
dict = new Dictionary(map)
assert dict.getValueForKey( 'string' ) == 'value'
assert dict.getValueForKey( 'number' ) == 3.14
assert dict.count() == 4
dict2 = new Dictionary(map)
assert dict == dict2

/*******************************************************************************
 * Test function as they'll be called by FileMaker
 ******************************************************************************/

// arry_Add( _array; _value )