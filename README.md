![Pollux](https://jitpack.io/v/Oclemy/Pollux.svg)
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
	        implementation 'com.github.Oclemy:Pollux:1.0.0'
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
       val adapter =  PolluxAdapter.with<User, ItemBinding>(R.layout.item) { adapterPosition, model, binding ->
                
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

So in summary you do the following:
1. Install the library
2. Enable data binding
3. Instantiate the adapter
4. Add data to the adapter.
5. Set the adapter to the recyclerview.

## Pagination

With Pollux, load more pagination is as easy as it can get. Not only do you get pagination but also a progress bar is shown at the bottom. The progressbar gets removed when you call `markLoadMoreAsComplete()`. You need to call that method when you've finished downloading data from the server.

All you need to get load more pagination is invoke the setLoadMorePaganination() method:

```kotlin
	adapter?.setupLoadMorePagination(rv) {
            //Download next page from server
            true
        }
```
The pagination works by informing you if you've reached the end of the list. The method you pass then gets invoked.

But how do I know the current page? Well it's simple. Just define a variable and assign it a value like say 1. Meaning the initial page is 1. Then every time end of the list is reached, increment it by 1.

For example something like this:
```kotlin
        adapter?.setupLoadMorePagination(rv) {
            simulateDownload(createItems()).observe(this@ExampleActivity, Observer {
                adapter?.addAll(it)
                adapter?.notifyDataSetChanged()
                adapter?.markLoadMoreAsComplete()
            })
            pageToFetch++
            true
        }
```

Check the sample project.

