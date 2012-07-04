import dictarray.*
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

/*******************************************************************************
 * Perform a simulation of calls from FileMaker as far as possible.
 *
 * _array is either blank or a JSON representation of an array.
 * _value is a string that stores either a string, a number, a dictionary or an
 *   array with dictionaries and arrays represented with JSON strings.
 * _index is a string storing an integer
 * _dict is either blank or a JSON representation of a dictionary.
 * _key is any string
 ******************************************************************************/

/*******************************************************************************
 * arry_Add( _array; _value )
 *
 * Should return a JSON string representing the array.
 ******************************************************************************/

String fm_array = FM.arry_Add( '', 'one' )
fm_array = FM.arry_Add( fm_array, 'two' )
fm_array = FM.arry_Add( fm_array, '3' )
assert FM.arry_Value( fm_array, '0' ) == 'one'
assert FM.arry_Value( fm_array, '1' ) == 'two'
assert FM.arry_Value( fm_array, '2' ) == '3'

/*******************************************************************************
 * arry_Count( _array )
 *
 * Should return an integer.
 ******************************************************************************/
 
 assert FM.arry_Count( fm_array ) == 3

/*******************************************************************************
 * arry_Head( _array )
 *
 * Should return a string that represents a string, number, dictionary or array,
 *   with the dictionary or array stored as a JSON string.
 ******************************************************************************/
 
 assert FM.arry_Head( fm_array ) == 'one'

/*******************************************************************************
 * arry_Tail( _array )
 *
 * Should return a string that represents a string, number, dictionary or array,
 *   with the dictionary or array stored as a JSON string.
 ******************************************************************************/

String tail_array = FM.arry_Add( FM.arry_Add( '', 'two' ), '3' )
assert FM.arry_Tail( fm_array ) == tail_array

/*******************************************************************************
 * arry_IsEmpty( _array )
 *
 * Should return a string that represents a string, number, dictionary or array,
 *   with the dictionary or array stored as a JSON string.
 ******************************************************************************/

assert FM.arry_IsEmpty( '' )
assert FM.arry_IsEmpty( '[]' )

assert FM.dict_IsEmpty( '' )
assert FM.dict_IsEmpty( '{}' )

String fm_dict = FM.dict_SetValueForKey( '', 'string', 'value' )
fm_dict = FM.dict_SetValueForKey( fm_dict, 'number', '3.14' )
assert FM.dict_GetValueForKey( fm_dict, 'string' ) == 'value'
assert FM.dict_GetValueForKey( fm_dict, 'number' ) == '3.14'

sub_dict = FM.dict_SetValueForKey( '', 'a', '1' )
sub_dict = FM.dict_SetValueForKey( sub_dict, 'b', '2' )
sub_dict = FM.dict_SetValueForKey( sub_dict, 'c', '3' )

fm_dict = FM.dict_SetValueForKey( fm_dict, 'dictionary', sub_dict )
fm_dict = FM.dict_SetValueForKey( fm_dict, 'array', fm_array )
assert FM.dict_GetValueForKey( fm_dict, 'dictionary' ) == sub_dict
assert FM.dict_GetValueForKey( fm_dict, 'array' ) == fm_array