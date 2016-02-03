# WeMeep Media Service
### Description
This service handles all the media (pictures, video, etc.). It's built over Docker containers and uses Java Spark, Gradle, PostgreSql and
Amazon S3 for storage.

### Setup
#### Docker
Simply:
```
docker-compose up -d
```
#### Environment variables
Set:
```
- PG_USER
- PG_PASS
```

#### Exposed ports
```
- PhpPgAdmin: 49161/phppgadmin
- API: 4567
```

### WebService
The web service exposes the following methods:

- Upload a profile picture with `POST`. Picture must be a form-data key-value entry, with
 key = "picture" and lighter than 800Kb:

```
http://host:4567/pictures/profile
Returns { "url": <picture url> } or { "Error": <some error>}
```


- Get a profile picture with `GET`:

```
http://host:4567/pictures/profile/<userId>
Returns  { "url": <picture url> } or { "Error": <some error>}
```

- Upload a comment picture with `POST`. Picture must be a form-data key-value entry, with
key = "picture" and lighter than 1.5Mb:

```
http://host:4567/pictures/comment/<commentId>
Returns { "url": <picture url>} or { "Error": <some error>}
```


- Get a comment picture with `GET`:

```
http://host:4567/pictures/comment/<commentId>

Returns  { "url": <picture url> } or { "Error": <some error>}
```


### Data model
#### Objects
##### Profile Picture
|  Field      |  Values   |
| :---------- | :-------- |
| userId      | String    |
| path        | String    |
| thumb_path  | String    |
| createdAt   | Timestamp |

##### Comment Picture
|  Field      |  Values   |
| :---------- | :-------- |
| commentId   | String    |
| rootMeepId  | String    |
| senderId    | String    |
| path        | String    |
| thumb_path  | String    |
| createdAt   | Timestamp |


## TODO
- Add authentication to the database
- Protect the API
- Check for SQLInjection