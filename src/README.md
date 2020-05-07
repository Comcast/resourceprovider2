Android Resource Provider 2
======================

A Gradle Plugin for Android Application Projects that generates a ResourceProvider class, and support classes, providing APIs which get Android Resources
 
ResourceProvider provides APIs for R.string, R.plurals, R.drawable, R.color, R.dimen and R.color elements.
The plugin generates the exact same output as the Resource Provider Annotation Processor, but is compatible with the Android
Gradle Plugin v3.6.0 and above.  
   
   Resource Provider allows the presentation layer in your MVP implementation to explicitly control presentation details
   without needing access or knowledge of Android's Context or R classes. ResourceProvider automatically generates an
   API for every string resource in your application, allowing the details of fetching resources to be opaque to 
   your presenters, maintaining strict separation of concerns. 
   
   ResourceProvider provides APIs that mirror the application's resource names, but in lower camel case and 
   with the standard underscore delimiter stripped.  For example, for the string resource
    
   ```xml
    <string name="one_arg_formatted_string">This format strings has %1$d args</string>
   ```

   resourceprovider will generate the API:
   
   ```java
   public String getOneArgFormattedString(Object... formatArgs) { ... }
   ```
   
   For any plural:
   
   ```xml
   <plurals name="days_until_friday">
        <item quantity="one">Only 1 day until Friday!</item>
        <item quantity="other">%d days until Friday</item>
   </plurals>
   ```
  
   resourceprovider will generate the API:
   
   ```Java
   public String getDaysUntilFridayQuantityString(int quantity, Object... formatArgs) { ... }
   ```
   
   And for any drawable file
   
   ```xml
    any_drawable.png ( or any_drawable.xml)
   ```

   resourceprovider will generate the API:
   
   ```java
   public Drawable getAnyDrawable() { ... } 
   ```
   
  Calling ResourceProvider APIS
  =============================
  In order to avoid conflicts with duplicate resource ids, ResourceProvider organizes its APIs into delegate providers for
  each resource type.  To call a ResourceProvider API, clients will make a call in the format:
  
  ```java
  resourceProvider.get<resource_type>().get<resource_name>()
  ```
  
  For example, to get a String resource:
  ```java
  resourceProvider.getStrings().getSomeString()
  ```
   
  And for a color
  ```java
  resourceProvider.getColors().getSomeColor()
  ```

Or in Kotlin, 

 ```kotlin
  resourceProvider.strings.getSomeString()
  resourceProvider.colors.someColor
  ```
  
  ResourceProvider only requires an application context for construction, so can easily be provided as a singleton by
    dependency injection, and can also be mocked for unit testing.
     
  Setup
  ======================
   
   To use ResourceProvider, in your project build.gradle add
   
   ```xml
   plugins {  
       id 'com.xfinity.resourceprovider' version '1.1.0'
   }
   ```
   
  Provider Generation Configs
  ======================  
  By default, the classes Resource Provider generates will be created in the top level package of the app.  For instance, if your app id  (specified in your
  build.gradle or AndroidManifest.xml) is "com.my.app", then the resource provider class will  be com.my.app.ResourceProvider.  You can change this in the
  "resourceprovider" closure in the app-level build.gradle file:  
  
   ```xml
  resourceprovider {
      packageName = "my.awesome.androidapp"
      generateStringProvider = false
      generateColorProvider = false
      generateDrawableProvider = false
      generateDimenProvider = false
      generateIdProvider = false
  }
  ```
 ResourceProvider will, also by default, generate a provider class with APIs to get all the resource types mentioned above, and integer IDs of all the supported resource types.  
 Since this will add a large number of APIs to the method count of your app, you can disable generation of any of the Provider Classes by configuring a closure in the
 build.gradle file.  See the example above for configuration of specific providers.