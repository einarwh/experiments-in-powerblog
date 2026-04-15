:page/title AI co-workers
:blog-post/tags [:tech ]
:blog-post/author {:person/id :einarwh}

<!-- :blog-post/published #time/ldt "2014-12-27T00:00:00" -->

:page/body

# Life in the JSON morgue

<p class="blog-post-date">April 8, 2026</p>

A couple of weeks ago, we paid a visit to the JSON morgue, which is my name for the peristence layer if you happen to store your data as JSON. I'd like to return with some bla bla. 

The basic problem of the JSON morgue is the passing of time. 

Image: Timeline which shows JSON documents being written at various points, and the schema for the documents changing on occasion. 



To some extent it is a problem of data migration. We could synchronize the deployment of a new version of our software (with the new schema for the documents) with a data migration effort. But at least for the duration of the data migration, this leaves our software in a problematic state. Or we could hope for the best, which is something that is done surprisingly often. That is, we make sure that the changes are minor.

