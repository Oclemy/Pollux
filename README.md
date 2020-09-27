# Pollux
Pollux is the easiest yet reliable recyclerview adapter you will ever find. Only two lines of code, no need to extend classes or create any viewholder. It uses data binding to generate code for you.

## Installation

The library is hosted in jitpack. So start by adding jitpack url as shown below in your project-level build.gradle:

```groovy
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

Then specify the dependency in your module-level build.gradle:

```groovy
	    dependencies {
	        implementation 'com.github.Oclemy:Pollux:Tag'
	    }
```

## Usage

Pollux is as simple to use as probably possible. Yet it gives you what you would typically expect in a recyclerview adapter.

Because it uses data binding, you have to enable data binding in your project. To do that go your app-level build.gradle file and add the following inside the android closure:

```groovy
    buildFeatures {
        dataBinding = true
    }
```
If you are using Kotlin then you may need to add kapt plugin within the same file at the top:
```groovy
apply plugin: 'kotlin-kapt'
```

Here is how to use it:

```kotlin
       val adapter =  PolluxAdapter.with<User, CardImageOverlayBottomTextBinding>(R.layout.item) { adapterPosition, model, binding ->
                
                //Then bind data here e.g
		loadImg(model.imageURL,binding.image)
                binding.titleTV.text = model.text1
                binding.image.setOnClickListener {
                    //Clicked, open detail page
                }
            }
        adapter?.addAll(createItems())
        rv.adapter = adapter
```

And that's it, seriously. No extending of any class, no creating of any viewholder or manual inflations. Pollux takes advantage of data binding.
