package brave.jms;

import brave.Span;
import brave.Tracer.SpanInScope;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueSender;

final class TracingQueueSender extends TracingMessageProducer implements QueueSender {

  TracingQueueSender(QueueSender delegate, JmsTracing jmsTracing) {
    super(delegate, jmsTracing);
  }

  @Override public Queue getQueue() throws JMSException {
    return ((QueueSender) delegate).getQueue();
  }

  @Override public void send(Queue queue, Message message) throws JMSException {
    Span span = createAndStartProducerSpan(null, message);
    SpanInScope ws = tracer.withSpanInScope(span); // animal-sniffer mistakes this for AutoCloseable
    try {
      ((QueueSender) delegate).send(queue, message);
    } catch (RuntimeException | JMSException | Error e) {
      span.error(e);
      throw e;
    } finally {
      ws.close();
      span.finish();
    }
  }

  @Override
  public void send(Queue queue, Message message, int deliveryMode, int priority, long timeToLive)
      throws JMSException {
    Span span = createAndStartProducerSpan(null, message);
    SpanInScope ws = tracer.withSpanInScope(span); // animal-sniffer mistakes this for AutoCloseable
    try {
      ((QueueSender) delegate).send(queue, message, deliveryMode, priority, timeToLive);
    } catch (RuntimeException | JMSException | Error e) {
      span.error(e);
      throw e;
    } finally {
      ws.close();
      span.finish();
    }
  }
}