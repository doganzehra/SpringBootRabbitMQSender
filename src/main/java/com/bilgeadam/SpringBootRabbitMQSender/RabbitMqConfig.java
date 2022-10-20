package com.bilgeadam.SpringBootRabbitMQSender;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate.ConfirmCallback;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// bütün import lar AMQP ile
// org.springframework.amqp.rabbit
@RestController
public class RabbitMqConfig
{
	private org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;

	@Bean
	private RabbitTemplate setReturnCallback(ConnectionFactory connectionFactory)
	{
		// bence aqnlamlı değil ama gönderdikten sora ne olduğunu bilmek için
		RabbitTemplate myCustomTemplate = new RabbitTemplate(connectionFactory);
		ConfirmCallback confirmCallback = new ConfirmCallback()
		{
			@Override
			public void confirm(CorrelationData correlationData, boolean ack, String cause)
			{
				if (correlationData == null)
				{
					return;
				}
				// This log is to be notified if the payload is succesfully delivered to rabbitmq
				// It acks true even if the message goes to deadletter queue
				// It doesn't return any data because we are not expecting a message from the consumer
				System.err.println("Correlation data: " + correlationData.toString());
				System.err.println("Ack: " + ack);
				System.err.println("Cause: " + cause);
				System.err.println();
			}
		};
		myCustomTemplate.setConfirmCallback(confirmCallback);
		return myCustomTemplate;
	}

	public RabbitMqConfig(RabbitTemplate rabbitTemplate)
	{
		this.rabbitTemplate = rabbitTemplate;
		// custom nesne gönderebilmek için
		this.rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
	}

	@GetMapping(path = "/sendPerson")
	public String sendPerson()
	{
		Person person = new Person(1, "numan");
		rabbitTemplate.convertAndSend("amq.direct", "directRK", person);
		return "gönderildi";
	}

	@GetMapping(path = "/send")
	public String send()
	{
		Message mesaj = MessageBuilder.withBody("Merhaba rabbitmq normal mesaj".getBytes()).build();
		rabbitTemplate.send("amq.direct", "directRK", mesaj);
		return "gönderildi";
	}
}

class Person
{
	private int id;

	private String isim;

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public String getIsim()
	{
		return isim;
	}

	public void setIsim(String isim)
	{
		this.isim = isim;
	}

	public Person()
	{
	}

	public Person(int id, String isim)
	{
		this.id = id;
		this.isim = isim;
	}
}