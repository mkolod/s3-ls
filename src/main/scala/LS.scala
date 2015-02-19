import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3
import com.amazonaws.services.s3.model.{ObjectListing, ListObjectsRequest}
import s3.AmazonS3Client
import scala.annotation.tailrec
import scala.collection.JavaConversions._

/**
 * List AWS S3 bucket contents
 * Chunk response to prevent time-out and blowing the heap
 */
object LS extends App {

  if (args.length != 3) {

    sys.error("Usage: LS accessKey secretKey bucketName")
  }

  case class Stats(sizeBytes: Long, objectCount: Long) {

    def +(other: Stats) = Stats(this.sizeBytes + other.sizeBytes, this.objectCount + other.objectCount)
  }


  def getStats(accessKey: String, secretKey: String, bucketName: String, maxKeysAtATime: Int = 1000): Stats = {

    val client = new AmazonS3Client(new BasicAWSCredentials(accessKey, secretKey))
    val request = new ListObjectsRequest().withBucketName(bucketName).withMaxKeys(maxKeysAtATime)

    // TODO: Rewrite this as Play Enumerators/Iteratees
    @tailrec
    def getPartialStats(stats: Stats, listing: ObjectListing): Stats = {

      val newStats = listing.getObjectSummaries.foldLeft(Stats(0L, 0L)) {
        case (Stats(size, count), summary) => Stats(size + summary.getSize, count + 1)
      } + stats

      println(s"Got ${newStats.objectCount} objects and ${newStats.sizeBytes} bytes so far")

      client.listNextBatchOfObjects(listing) match {
        case next if next.getObjectSummaries.size > 0 =>
          getPartialStats(newStats, next)
        case _ => newStats
      }
    }

    getPartialStats(Stats(0L, 0L), client.listObjects(request))

  }

  val start = System.currentTimeMillis

  val (accessKey, secretKey, bucketName) = args.take(3)
  val stats = getStats(accessKey, secretKey, bucketName)

  val timeSecs = (System.currentTimeMillis - start) / 1000L
  val timeMins = timeSecs / 60L

  val totalSizeMiB = stats.sizeBytes / (math.pow(1024, 2).toLong)
  val totalSizeGiB = totalSizeMiB / 1024
  val totalSizeTiB = totalSizeGiB / 1024

  println(s"Number of objects = ${stats.objectCount}")
  println(s"Total size = $totalSizeMiB MB")
  println(s"Total time secs = $timeSecs")
  println(s"Total time mins = $timeMins")

}
