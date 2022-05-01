# Music Service

## How to run it
From the root of the project run: `./gradlew bootRun`

This will start the service on port `8081`.

To test you can use these sample requests:

- `curl -X GET "http://localhost:8081/musify/music-artist/details/f27ec8db-af05-4f36-916e-3d57f91ecf5e"` (Michael Jackson)
- `curl -X GET "http://localhost:8081/musify/music-artist/details/9c9f1380-2516-4fc9-a3e6-f9f61941d090"` (Muse)

## Tools used and why

### Service
I used Spring Webflux to avoid idle threads while waiting for responses from the external REST APIs used here. Leveraging Kotlin Co-routines, the code is written in a synchronous style while still executing requests in parallel (multiple album cover images are fetched in parallel, album covers are fetched in parallel to resolving and fetching the description).

To avoid too many parallel requests for cover art, I used a simple semaphore. 

### Testing
I used WireMock to create a test setup that avoids testing the implementation and facilitates refactoring the code as needed. A full expected response is tested against the actual response, ensuring data from all external APIs is used correctly.

With more time, I would add more lower-level unit tests to cover edge / error cases like individual requests failing or returning incomplete data.

## Scalability
While using a reactive approach helps a lot for this type of use case, there are a number of things that would help further improve the scalability of the solution, most importantly asynchronous data ingestion and caching.

Ideally, the data would not be fetched from 3rd party APIs at request time, but rather ingested asynchronously via a message queue or regular export. Failing that, the results could be cached.

Since the response does not vary for a single MBID, the full response could be cached for a time. However, this could be problematic in cases where the response is incomplete due to e.g. the album cover art archive not responding. To avoid this, it may be more effective to cache the individual elements.

Finally, the service could easily be scaled horizontally since it is stateless aside from a potential cache (which could be shared across instances).

## Other non-functional requirements
I have only implemented very rudimentary error handling for the cover art archive API (as it is not very reliable). More thorough error handling and additional tests would be needed for a fully production-ready application.  
