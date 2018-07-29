# Getting Started

### Templates
Arguably the most powerful feature of Cardinal is the request templating.

Cardinal treats requests as templates. Before sending a request Cardinal processes keywords and functions present in the request template and replace them with appropriate values.

![Cardinal Curl View](images/cardinal_curl.png)

In the above example, a request template to create a user was provided on the left. Each time "As cURL" is clicked, the template is processed, the keywords/functions are exchanged for values and the cURL command is built up.

#### Variables
Cardinal comes with a list of predefined variables. Each variable is instantiated with a value during the processing of a request; if, for example, you use the #{firstName} variable more than once in a single request template, it will have the same value each time it is referenced. If a unique first name (for example) is needed, you can use one of the random functions mentioned in the next section.

| Variable   	            | Example   	|
|---	                    |---	        |
| `#{guid}` 	            | f17c23de 	    |
| `#{int}`    	            | 1118345848    |
| `#{float}`                | 0.91777223    |
| `#{firstName}`            | Dierdre    |
| `#{lastName}`             | Verona    |
| `#{action}`               | Repair    |
| `#{businessEntity}`       | PLC    |
| `#{communication}`        | Call    |
| `#{country}`              | Japan    |
| `#{object}`               | Car    |
| `#{place}`                | Hospital    |
| `#{emoji}`                | 🍏 |

#### Functions

Cardinal has a few limited functions that help with string manipulation and entropy.

| Function   	                    | Example   	|
|---	                            |---	        |
| `#{randomGuid()}` 	            | b2519c1f 	    |
| `#{randomInt()}`    	            | 1043257081    |
| `#{randomFloat()}`                | 0.5201468    |
| `#{randomFirstName()}`            | Juliana    |
| `#{randomLastName()}`             | Giuseppina    |
| `#{randomAction()}`               | Question    |
| `#{randomBusinessEntity()}`       | CIO    |
| `#{randomCommunication()}`        | Email    |
| `#{randomCountry()}`              | Denmark    |
| `#{randomObject()}`               | Armchair    |
| `#{randomPlace()}`                | Supermarket    |
| `#{randomEmoji()}`                | 🚗 |
| `#{random("A", "B", "C")}`        | A    |
| `#{randomBetween(20, 50)}`        | 45    |
| `#{lower("HELLO")}`               | hello    |
| `#{upper("hello")}`               | HELLO    |
| `#{capitalise("hello")}`          | Hello |
| `#{lorem(4)}`                     | Lorem ipsum dolor sit |

You can also combine variables and functions, for example:
```
#{random(lower("HELLO"), upper("world"))} => hello|WORLD
#{lorem(randomBetween(1, 3))} =>  Lorem|Lorem ipsum|Lorem ipsum dolor
#{capitalise(random("hello", "world"))} => Hello|World
```

The quickest way to make sure your templates are being processed as expected is to output as cURL. Once you're happy, send a single request and then move on to sending the bulk requests. 



  

 