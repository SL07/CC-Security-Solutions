#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "altera_up_avalon_audio_and_video_config.h"
#include "altera_up_avalon_audio.h"
#include "altera_up_sd_card_avalon_interface.h"
#include "altera_up_avalon_rs232.h"
#include <sys/alt_irq.h>

//General
alt_up_audio_dev *audio;
short int opened_file = 0;
int sample_size = 110; //ISR: 96 Hit: 2

//Music
unsigned int *audio_data_16_music;
int audio_data_16_music_count = 0;
int music_file_size = 0;
int audio_data_16_music_current_index = 0;

//Hit
unsigned int *audio_data_16_hit;
int audio_data_16_hit_count = 0;
int hit_file_size = 0;
int audio_data_16_hit_current_index = 0;

//ISR
char *filename;
unsigned int *audio_buffer;

//RS232
alt_up_rs232_dev *uart;

//Alarm Flags
int flag = 0;
int img_count = 1;

void audio_isr(void* context, alt_u32 id)
{
	int audio_buffer_count;

	if(flag) //Flag trigger
	{
		for(audio_buffer_count = 0 ; audio_buffer_count < sample_size; audio_buffer_count++)
		{
			if(audio_data_16_hit_current_index >= audio_data_16_hit_count)
			{
				audio_buffer[audio_buffer_count] = audio_data_16_music[audio_data_16_music_current_index];

				if(audio_data_16_music_current_index >= audio_data_16_music_count)
					audio_data_16_music_current_index = 0;
				else
					audio_data_16_music_current_index++;
			}
			else
			{
				audio_buffer[audio_buffer_count] = (audio_data_16_music[audio_data_16_music_current_index] >> 1) + (audio_data_16_hit[audio_data_16_hit_current_index] );

				audio_data_16_hit_current_index++;

				if(audio_data_16_music_current_index >= audio_data_16_music_count)
					audio_data_16_music_current_index = 0;
				else
					audio_data_16_music_current_index++;

				if(audio_data_16_hit_current_index >= audio_data_16_hit_count)
					audio_data_16_hit_current_index = 0;
				else
					audio_data_16_hit_current_index++;

			}

		}
	}

	else
	{
		for(audio_buffer_count = 0 ; audio_buffer_count < sample_size; audio_buffer_count++)
		{
			audio_data_16_hit_current_index = 0;

			audio_buffer[audio_buffer_count] = audio_data_16_music[audio_data_16_music_current_index];

			if(audio_data_16_music_current_index >= audio_data_16_music_count)
				audio_data_16_music_current_index = 0;
			else
				audio_data_16_music_current_index++;
		}
	}

	alt_up_audio_write_fifo(audio, audio_buffer, sample_size, ALT_UP_AUDIO_LEFT);
	alt_up_audio_write_fifo(audio, audio_buffer, sample_size, ALT_UP_AUDIO_RIGHT);
}

short convert_to_16 (char a, char b)
{
	short c = (unsigned char) a << 8 | (unsigned char) b;

	return c;
}

void read_sd (void)
{
	int connected = 0;
	int exist = 0;
	short int file_found = 0;
	char *file_name;

	alt_up_sd_card_dev *device_reference = NULL;
	device_reference = alt_up_sd_card_open_dev(SD_CARD_0_NAME);

	if (device_reference != NULL)
	{
		if ((connected == 0) && (alt_up_sd_card_is_Present()))
		{
			printf("Card connected.\n");

			if (alt_up_sd_card_is_FAT16())
				printf("FAT16 file system detected.\n");
			else
				printf("Unknown file system.\n");

			connected = 1;

			file_found = alt_up_sd_card_find_first ("//", file_name);

			while (file_found == 0)
			{
				file_found = alt_up_sd_card_find_next(file_name);

				if (file_found == 0)
				{
					exist = 1;
					//printf ("%s\n", file_name);
				}

				else if (file_found == 1)
					printf ("Invalid directory.\n");

				else if (file_found == -1 && exist == 0)
					printf ("Card empty.\n");
			}
		}

		else if ((connected == 1) && (alt_up_sd_card_is_Present() == false))
		{
			printf("Card disconnected.\n");

			connected = 0;
		}
	}

	//printf ("Error, please check card.\n");
}

void open_file(filename)
{
	opened_file = alt_up_sd_card_fopen(filename, 0);

	if (opened_file == -1)
		printf("File could not be opened\n");

	if (opened_file == -2)
		printf("File is already opened\n");

	printf("File is opened.\n");
}

void reverse_array(int array[], int count)
{
	int temp, i;

	for (i = 0; i < count/2; ++i)
	{
		temp = array[i];
		array[i] = array[count-i-1];
		array[count-i-1] = temp;
	}
}

void audio_file_info()
{
	unsigned int audio_file_header[40];
	unsigned int audio_file_size[4];

	int i;
	int j = 0;


	for(i = 0; i < 40; i++)
		audio_file_header[i] = alt_up_sd_card_read(opened_file);


	for(i = 4; i < 8; i++ )
	{
		audio_file_size[j] = audio_file_header[i];

		j++;
	}

	reverse_array(audio_file_size, 4);

	if(filename == "alarm.wav")
		hit_file_size = (audio_file_size[0] << 8*3 | audio_file_size[1] << 8*2 | audio_file_size[2] << 8 | audio_file_size[3]) + 8;
	else
		music_file_size = (audio_file_size[0] << 8*3 | audio_file_size[1] << 8*2 | audio_file_size[2] << 8 | audio_file_size[3]) + 8;

	printf("empty size: %d\n", music_file_size);
	printf("alarm size: %d\n", hit_file_size);
}

void audio_data_load()
{
	int i,j;
	unsigned int audio_data_byte[2];

	audio_buffer = (unsigned int *) malloc (sample_size * sizeof(unsigned int));

	if(filename == "alarm.wav")
	{
		audio_data_16_hit = (unsigned int *) malloc ((hit_file_size/2) * sizeof(unsigned int));

		printf("Loading hit data\n");

		for(i = 0; i < (hit_file_size/2); i++)
		{
			for(j = 0; j < 2; j++)
				audio_data_byte[j] = alt_up_sd_card_read(opened_file) & (0x00FF);

			audio_data_16_hit[audio_data_16_hit_count] = convert_to_16(audio_data_byte[1], audio_data_byte[0]);
			audio_data_16_hit_count++;
		}

		printf("Finish loading hit data\n");
	}
	else
	{
		audio_data_16_music = (unsigned int *) malloc ((music_file_size/2) * sizeof(unsigned int));

		printf("Loading music data\n");

		for(i = 0; i < (music_file_size/2); i++)
		{
			for(j = 0; j < 2; j++)
				audio_data_byte[j] = alt_up_sd_card_read(opened_file) & (0x00FF);

			audio_data_16_music[audio_data_16_music_count] = convert_to_16(audio_data_byte[1], audio_data_byte[0]);
			audio_data_16_music_count++;
		}

		printf("Finish loading music data\n");
	}
}


void audio_initialize()
{
	audio = alt_up_audio_open_dev(AUDIO_0_NAME);

	alt_up_audio_reset_audio_core(audio);

	int i;

	audio_buffer = (unsigned int *) malloc (sample_size * sizeof(unsigned int));

	for(i = 0 ; i < sample_size; i++)
	{
		audio_buffer[i] = audio_data_16_music[audio_data_16_music_current_index];

		if(audio_data_16_music_current_index >= audio_data_16_music_count)
			audio_data_16_music_current_index = 0;
		else
			audio_data_16_music_current_index++;
	}
}

void start_audio_interrupt()
{
	if(alt_irq_register(AUDIO_0_IRQ, NULL ,audio_isr) == 0)
		printf("irq_register success\n");
	else
		printf("irq_register fail\n");

	printf("Enable Interrupt\n");
	alt_up_audio_enable_write_interrupt(audio);
}

void stop_audio_interrupt()
{
	alt_up_audio_disable_write_interrupt(audio);
}

void load_music(char *selectedFile)
{
	filename = selectedFile;
	read_sd();
	open_file(filename);
	audio_file_info();
	audio_data_load();
}

void recieve_data()
{
	int i;
	unsigned char data;
	unsigned char parity;
	short int output_file;
	char filename[256];
	char *image;
	unsigned char message[] = "Picture received";

	printf("UART Initialization\n");
	alt_up_rs232_dev* uart = alt_up_rs232_open_dev(RS232_0_NAME);

	printf("Clearing read buffer to start\n");

	while (alt_up_rs232_get_used_space_in_read_FIFO(uart)) {
		alt_up_rs232_read_data(uart, &data, &parity);
	}

	printf("Waiting for data to come back from the Middleman\n");

	while (alt_up_rs232_get_used_space_in_read_FIFO(uart) == 0);

	flag = 1;

	// First four byte is the number of characters in our message

	int num_to_receive = 0;
	unsigned int data_byte[4];

	//4 Byte
	for(i = 0; i < 4; i++)
	{
		while (alt_up_rs232_get_used_space_in_read_FIFO(uart) == 0) ;
		alt_up_rs232_read_data(uart, &data, &parity);
		data_byte[i] = data;

		printf("%x ", data_byte[i]);
	}

	num_to_receive = (data_byte[0] << 8*3 | data_byte[1] << 8*2 | data_byte[2] << 8 | data_byte[3]);
	image = (char*) malloc (num_to_receive*sizeof(char));

	printf("About to receive %d characters:\n", num_to_receive);

	for (i = 0; i < num_to_receive; i++) {
		while (alt_up_rs232_get_used_space_in_read_FIFO(uart) == 0) ;
		alt_up_rs232_read_data(uart, &data, &parity);
		image[i] = data;
	}

	printf("Finish Reading Data\n");

	printf("Sending the message to the Middleman\n");

	// Start with the number of bytes in our message
	alt_up_rs232_write_data(uart, (unsigned char) strlen(message));

	// Now send the actual message to the Middleman

	for (i = 0; i < strlen(message); i++) {
		alt_up_rs232_write_data(uart, message[i]);
	}

	sprintf(filename, "IMG_%d", img_count);
	strcat(filename, ".jpg");

	printf ("%s\n", filename);

	output_file = alt_up_sd_card_fopen(filename, true);

	if (output_file < 0){
		printf ("Failed to create or open file.\n");
	}
	printf ("Writing new file.\n");
	for (i = 0; i < num_to_receive; i++){
		if (alt_up_sd_card_write(output_file,image[i]) == false){
			printf ("Error in writing file! Abort!\n");
		}
	}
	printf ("Finished writing.\n");
	alt_up_sd_card_fclose (output_file);

	flag = 0;
	img_count++;
	free(image);
}

int main(void)
{
	load_music("alarm.wav");
	load_music("empty.wav");

	audio_initialize();
	start_audio_interrupt();

	int i;
	unsigned char data;
	unsigned char parity;
	short int output_file;
	char filename[256];
	char *image;
	unsigned char message[] = "Picture received";

	printf("UART Initialization\n");
	alt_up_rs232_dev* uart = alt_up_rs232_open_dev(RS232_0_NAME);

	printf("Clearing read buffer to start\n");

	while (alt_up_rs232_get_used_space_in_read_FIFO(uart)) {
		alt_up_rs232_read_data(uart, &data, &parity);
	}

	while (1){
		printf("Waiting for data to come back from the Middleman\n");

		while (alt_up_rs232_get_used_space_in_read_FIFO(uart) == 0);

		flag = 1;

		// First four byte is the number of characters in our message

		int num_to_receive = 0;
		unsigned int data_byte[4];

		//4 Byte
		for(i = 0; i < 4; i++)
		{
			while (alt_up_rs232_get_used_space_in_read_FIFO(uart) == 0) ;
			alt_up_rs232_read_data(uart, &data, &parity);
			data_byte[i] = data;

			printf("%x ", data_byte[i]);
		}

		num_to_receive = (data_byte[0] << 8*3 | data_byte[1] << 8*2 | data_byte[2] << 8 | data_byte[3]);
		image = (char*) malloc (num_to_receive*sizeof(char));

		printf("About to receive %d characters:\n", num_to_receive);

		for (i = 0; i < num_to_receive; i++) {
			while (alt_up_rs232_get_used_space_in_read_FIFO(uart) == 0) ;
			alt_up_rs232_read_data(uart, &data, &parity);
			image[i] = data;
		}

		printf("Finish Reading Data\n");

		printf("Sending the message to the Middleman\n");

		// Start with the number of bytes in our message
		alt_up_rs232_write_data(uart, (unsigned char) strlen(message));

		// Now send the actual message to the Middleman

		for (i = 0; i < strlen(message); i++) {
			alt_up_rs232_write_data(uart, message[i]);
		}

		sprintf(filename, "IMG_%d", img_count);
		strcat(filename, ".jpg");

		printf ("%s\n", filename);

		output_file = alt_up_sd_card_fopen(filename, true);

		if (output_file < 0){
			printf ("Failed to create or open file.\n");
		}
		printf ("Writing new file.\n");
		for (i = 0; i < num_to_receive; i++){
			if (alt_up_sd_card_write(output_file,image[i]) == false){
				printf ("Error in writing file! Abort!\n");
			}
		}
		printf ("Finished writing.\n");
		alt_up_sd_card_fclose (output_file);

		flag = 0;
		img_count++;
		free(image);
	}
}
