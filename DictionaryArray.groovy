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
  
  /**---------------------------------------------------------------------------
   * Empty initiator, this doesn't work without it, don't know enough about
   * Groovy objects to know why. When I remove it, I get an error when
   * attempting to create a new Array (subclass of Collect) with a List
   * parameter.
   */
  def Collect() {
    
  }

  /**---------------------------------------------------------------------------
   * Initiate the Collect object with a string that was received as output from
   * another Collect object.
   */
  def Collect( String input ) {
    this.object = new JsonSlurper().parseText( input )
  }
  
  /**---------------------------------------------------------------------------
   * Assume that if a string value is passed and it looks like something else
   * (number, array or dictionary), it should be treated as that instead of a
   * string.
   */
  def convertValue( value ) {
    if ( value instanceof String ) {
      if ( value.isInteger() ) {
        value = value.toInteger()
      } else if ( value.isDouble() ) {
        value = value.toDouble()
      } else if ( value.startsWith( ['['] )
                | value.startsWith( ['{'] ) ) {
        value = new JsonSlurper().parseText( value )
      }
    }
    
    return value
  }
  
  /**---------------------------------------------------------------------------
   * Convert the object (a List or a Map) into a JSON representation.
   */
  String getOutput() {
    return new JsonBuilder( this.object ).toPrettyString()
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
    this.object = []
  }
  
  /**---------------------------------------------------------------------------
   * Initialize an Array with a List, converting any values as needed.
   */
  def Array( List list ) {
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
  void add( value ) {
    this.object.add( this.convertValue( value ) )
  }
  
  /**---------------------------------------------------------------------------
   * Return the first value in the list.
   */
  def head() {
    return this.object.head()
  }
  
  /**---------------------------------------------------------------------------
   * Return a new list with everything except the first value.
   */
  def tail() {
    return new JsonBuilder( this.object.tail() ).toPrettyString()
  }
  
  /**---------------------------------------------------------------------------
   * Return the value at the specified index.
   */
  def value( index ) {
    return this.object[ index ]
  }
  
  /**---------------------------------------------------------------------------
   * Return true if there are no values in the Array.
   */
  def isEmpty() {
    return this.count() == 0
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
   * Set the value for the specified key, overriding any existing value if it
   * exists.
   */
  def setValueForKey( key, value ) {
    
  }
  
  /**---------------------------------------------------------------------------
   * Return the value stored for the specified key.
   */
  def getValueForKey( key ) {
    
  }
  
  /**---------------------------------------------------------------------------
   * Return true if there are no key/value pairs in the Dictionary.
   */
  def isEmpty() {
    
  }
}

Collect col = new Collect()
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

JsonBuilder json
String output

def map = [ 'string':'a string', 'number':3.14 ]
json = new JsonBuilder( map )
output = json.toPrettyString()
col = new Collect( output )
assert col.output == output

def list = ['a','b','c',4,5,6]
json = new JsonBuilder( list )
output = json.toPrettyString()
col = new Collect( output )
assert col.output == output

assert col.count() == 6