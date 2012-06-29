import dictarray.*
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper


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
 * Sandbox
 ******************************************************************************/
