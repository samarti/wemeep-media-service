# WeMeep Media Service
### Description
This service handles all the user session info, like all their logged devices, session tokens and so on. It's built over Docker containers and uses Java Spark, Gradle and PostgreSql.

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

- Upload a profile picture with `POST`:

```
http://host:4567/pictures/profile

Data: { body: { userId:<someId> } }
Returns { "url": <picture url>, "thumb_url": <thumb url> }
```


- Get a profile picture with `GET`:

```
http://host:4567/pictures/profile/<userId>

Returns  { "url": <picture url>, "thumb_url": <thumb url> }
```

- Upload a comment picture with `POST`:

```
http://host:4567/pictures/comment

Data: { body: { senderId:<someId>, rootMeepId: <someId>, commentId:<someId>} }
Returns { "url": <picture url>, "thumb_url": <thumb url> }
```


- Get a comment picture with `GET`:

```
http://host:4567/pictures/comment/<commentId>

Returns  { "url": <picture url>, "thumb_url": <thumb url> }
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