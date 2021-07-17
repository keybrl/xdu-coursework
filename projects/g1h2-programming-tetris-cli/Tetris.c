#include <stdio.h>
#include <windows.h>
#include <conio.h>
#include <time.h>

#define row 20
#define col 10

void move(int direction);  // direction: 1 => right, -1 => left.
void rotate(int direction);  // direction: 1 => right(clockwise), -1 => left(anti-clockwise).
int softDrop();  // It has the same return as moveDown().
int hardDrop();  // return: -1 => can't drop and fail, 1 => okey and creat a new block.
void hold();

int moveDown();  // return: -1 => can't move and fail, 0 => okey, 1 => can't move and creat a new block.
void cleanLine(int lineID);
void printFrame();
void creatBlock();

int winSize(int height,int width);

// Hold
// Pause
// Soft Drop
// Hard Drop

int movingBlock[row+2][col], tempBlock[row+2][col];
int space[row+2][col];

int sleepTime = 400;
int runningState = 0;  // -1:fail; 0:undefined; 1:running; 2:pause; 3:exit.
float B2B = 1;
int holdState = 0;

int level = 0, lines = 0, score = 0;
int holdBlock = 0, nowBlock = 0, nextBlock[4] = {0, 0, 0};  // 0:undefined; 1:I; 2:J; 3:L; 4:O; 5:S; 6:T; 7:Z.


int main()
{
	int ms = 0;
	int i, j;
	char command;
	srand(time(NULL));
	winSize(24, 56);
	
	while(runningState != 3){
		printFrame();

		if(runningState == 0){  // 初始化游戏环境
			for(i = 0;i < row+2;i++){
				for(j = 0;j < col;j++){
					movingBlock[i][j] = space[i][j] = tempBlock[i][j] = 0;
				}
			}
			score = lines = 0;
			level = 1;
			B2B = 1;
			holdBlock = 0;
			holdState = 0;
			nextBlock[0] = nextBlock[1] = 1+(int)((7.0)*rand()/(RAND_MAX+1.0));
			nextBlock[2] = 1+(int)((7.0)*rand()/(RAND_MAX+1.0));
			nextBlock[3] = 1+(int)((7.0)*rand()/(RAND_MAX+1.0));
			creatBlock();
		}

		while(runningState == 0){
			if(_kbhit()){
				command = getch();
				if(command == 's')  // Start
					runningState = 1;
				else if(command == 'q')  // Quit
					runningState = 3;
			}
			Sleep(40);
		}

		while(runningState == 2){
			if(_kbhit()){
				command = getch();
				if(command == 'p')  // Return
					runningState = 1;
				else if(command == 'q')  // Quit
					runningState = 3;
				else if(command == 'r')  // Restart
					runningState = 0;
			}
			Sleep(40);
		}

		while(runningState == -1){
			if(_kbhit()){
				command = getch();
				if(command == 'r')  // Restart
					runningState = 0;
				else if(command == 'q')  // Quit
					runningState = 3;
			}
			Sleep(40);
		}

		while(runningState == 1){
			if(ms >= sleepTime - level * 50){
				ms = 0;
				runningState = moveDown();
				if(runningState == -1)
					break;
				else
					runningState = 1;
				printFrame();
			}
			if(_kbhit()){
				command = getch();
				if(command == 72)  // Rotate Right
					rotate(1);
				else if(command == 75)  // Move Left
					move(-1);
				else if(command == 77)  // Move Right
					move(1);
				else if(command == 80){  // SOFT DROP
					if(softDrop() == -1){
						runningState = -1;
						break;
					}
				}
				else if(command == ' '){  // HARD DROP
					if(hardDrop() == -1){
						runningState = -1;
						break;
					}
				}
				else if(command == 'c')  // HOLD
					hold();
				else if(command == 'z')  // Rotate Left
					rotate(-1);
				else if(command == 'p'){  // Pause
					runningState = 2;
				}
			}
			Sleep(1);
			ms += 1;
		}
	}
	return 0;
}


void move(int direction)
{
	int i, j, a, b;
	if(direction == 1){
		a = col-1;
		b = 0;
	}
	else{
		a = 0;
		b = col-1;
	}

	for(i = 0;i < row+2;i++){
		if(movingBlock[i][a] != 0)
			return;
		for(j = (1+direction)/2;j < col-(1-direction)/2;j++){
			if(movingBlock[i][j-direction] != 0 && space[i][j] !=0)
				return;
			tempBlock[i][j] = movingBlock[i][j-direction];
		}
	}
	for(i = 0;i < row+2;i++)
		tempBlock[i][b] = 0;
	for(i = 0;i < row+2;i++){
		for(j = 0;j < col;j++)
			movingBlock[i][j] = tempBlock[i][j];
	}
	printFrame();
	return;
}

void rotate(int direction)
{
	int i, j;
	int centerX = -1, centerY = -1;
	int newX, newY;
	// Search center and reset tempBlock.
	for(i = 0;i < row+2;i++){
		for(j = 0;j < col;j++){
			if(movingBlock[i][j] == 3){
				centerX = j;
				centerY = i;
			}
			tempBlock[i][j] = 0;
		}
	}
	if(centerX == -1)  // Have no center. (The O-block)
		return;
	for(i = 0;i < row+2;i++){
		for(j = 0;j < col;j++){
			if(movingBlock[i][j]!=0){
				// The coordinate transformation of rotate.
				newY = centerY - direction * (centerX - j);
				newX = centerX + direction * (centerY - i);
				if(newY < 0 || newY > row + 1 || newX < 0 || newX > col - 1)  // Beside the edge.
					return;
				else if(space[newY][newX] != 0)  // Beside the other block.
					return;
				else
					tempBlock[newY][newX] = movingBlock[i][j];
			}
		}
	}
	for(i = 0;i < row+2;i++){
		for(j = 0;j < col;j++){
			movingBlock[i][j] = tempBlock[i][j];
		}
	}
	printFrame();
	return;
}

int softDrop()
{
	int downState = moveDown();
	if(downState == 0){
		score += level;
		printFrame();
	}
	return downState;
}

int hardDrop()
{
	int downState;
	downState = moveDown();
	while(downState == 0){
		score += 2 * level;
		downState = moveDown();
	}
	printFrame();
	return downState;
}

void hold(){
	int i, j;
	if(holdState == 0){
		if(holdBlock == 0){
			holdBlock = nowBlock;
			for(i = 0;i < row+2;i++){
				for(j = 0;j < col;j++)
					movingBlock[i][j] = 0;
			}
			creatBlock();
			holdState = 1;
		}
		else{
			nextBlock[0] = holdBlock;
			holdBlock = nowBlock;
			for(i = 0;i < row+2;i++){
				for(j = 0;j < col;j++)
					movingBlock[i][j] = 0;
			}
			creatBlock();
			holdState = 1;
		}
	}
	else
		return;
}

int moveDown()
{
	int i, j, count = 0, clean = 0;
	for(j = 0;j < col;j++){
		if(movingBlock[row+1][j] != 0){
			i = 100;
			break;
		}
		for(i = 1;i < row+2;i++){
			if(movingBlock[i-1][j] != 0 && space[i][j] !=0){
				i = 100;
				j = 99;
				break;
			}
			tempBlock[i][j] = movingBlock[i-1][j];
		}
	}

	if(i == 100){
		for(i = 0;i < row+2;i++){
			for(j = 0;j < col;j++){
				if(movingBlock[i][j] != 0){
					space[i][j] = 1;
					movingBlock[i][j] = 0;
				}
			}
		}
		for(i = 0;i < row+2;i++){
			for(j = 0;j < col;j++){
				if(space[i][j] != 0)
					count++;
			}
			if(count==col){
				lines += 1;
				level = lines / 10 + 1;
				clean += 1;
				cleanLine(i);
			}
			count = 0;
		}
		if(clean == 1){
			score += 100 * level;
			B2B = 1;
		}
		else if(clean == 2){
			score += 300 * level;
			B2B = 1;
		}
		else if(clean == 3){
			score += 400 * level;
			B2B = 1;
		}
		else if(clean == 4){
			score += 800 * level * B2B;
			B2B = 1.5;
		}
		printFrame();
		for(i = 0;i < 2;i++){
			for(j = 0;j < col;j++){
				if(space[i][j] != 0){
					runningState = -1;
					return -1;
				}
			}
		}
		creatBlock();
		return 1;
	}

	for(j = 0;j < col;j++)
		tempBlock[0][j] = 0;
	for(i = 0;i < row+2;i++){
		for(j = 0;j < col;j++)
			movingBlock[i][j] = tempBlock[i][j];
	}
	return 0;
}
void cleanLine(int lineID)
{
	int i, j;
	for(i = lineID;i > 0;i--){
		for(j = 0;j < col;j++)
			space[i][j] = space[i-1][j];
	}
	return;
}

void printFrame()
{
	int i, j;
	system("cls");


	// The first line.
	printf(" ┏━━━━━━━━━━┓ ┏");
	for(i = 0;i < col;i++)
		printf("━━");
	printf("┓ ┏━━━━━━━━━━┓\n");


	// The line in middle.
	for(i = 2;i < row + 2;i++){
		// The left list.
		if(i == 2)
			printf(" ┃   HOLD   ┃ ┃");
		else if(i == 4){
			if(runningState == 1){
				if(holdBlock == 1)
					printf(" ┃          ┃ ┃");
				else if(holdBlock == 2)
					printf(" ┃   █     ┃ ┃");
				else if(holdBlock == 3)
					printf(" ┃       █ ┃ ┃");
				else if(holdBlock == 4)
					printf(" ┃   ██   ┃ ┃");
				else if(holdBlock == 5)
					printf(" ┃     ██ ┃ ┃");
				else if(holdBlock == 6)
					printf(" ┃     █   ┃ ┃");
				else if(holdBlock == 7)
					printf(" ┃   ██   ┃ ┃");
				else
					printf(" ┃          ┃ ┃");
			}
			else
				printf(" ┃          ┃ ┃");
		}
		else if(i == 5){
			if(runningState == 1){
				if(holdBlock == 1)
					printf(" ┃ ████ ┃ ┃");
				else if(holdBlock == 2)
					printf(" ┃   ███ ┃ ┃");
				else if(holdBlock == 3)
					printf(" ┃   ███ ┃ ┃");
				else if(holdBlock == 4)
					printf(" ┃   ██   ┃ ┃");
				else if(holdBlock == 5)
					printf(" ┃   ██   ┃ ┃");
				else if(holdBlock == 6)
					printf(" ┃   ███ ┃ ┃");
				else if(holdBlock == 7)
					printf(" ┃     ██ ┃ ┃");
				else
					printf(" ┃          ┃ ┃");
			}
			else
				printf(" ┃          ┃ ┃");
		}
		else if(i == 7 || i == 19)
			printf(" ┗━━━━━━━━━━┛ ┃");
		else if(i == 10)
			printf(" ┏━━━━━━━━━━┓ ┃");
		else if(i == 11)
			printf(" ┃  SCORE   ┃ ┃");
		else if(i == 12){
			if(runningState == 1)
				printf(" ┃ %-8d ┃ ┃", score);
			else
				printf(" ┃          ┃ ┃");
		}
		else if(i == 14)
			printf(" ┃  LEVEL   ┃ ┃");
		else if(i == 15){
			if(runningState == 1)
				printf(" ┃  %3d     ┃ ┃", level);
			else
				printf(" ┃          ┃ ┃");
		}
		else if(i == 17)
			printf(" ┃  LINES   ┃ ┃");
		else if(i == 18){
			if(runningState == 1)
				printf(" ┃  %4d    ┃ ┃", lines);
			else
				printf(" ┃          ┃ ┃");
		}
		else if(i == 3 || i == 6 || i == 13 || i == 16)
			printf(" ┃          ┃ ┃");
		else
			printf("              ┃");

		// The middle list
		if(runningState == -1){
			if(i == 2)      printf(" Game Ove!          ");
			else if(i == 4) printf(" Control Options:   ");
			else if(i == 5) printf(" %-19d", score);
			else if(i == 7) printf(" You can press:     ");
			else if(i == 8) printf(" r: Restart         ");
			else if(i == 9) printf(" q: Quit            ");
			else{
				for(j = 0;j < col;j++)
					printf("  ");
			}
		}
		else if(runningState == 0){
			if(i == 2)       printf(" Tetris             ");
			else if(i == 4)  printf(" Control Options:   ");
			else if(i == 5)  printf(" Left: Move Left    ");
			else if(i == 6)  printf(" Right: Move Right  ");
			else if(i == 7)  printf(" Up:   Rotate Right ");
			else if(i == 8)  printf(" Down: SOFT DROP    ");
			else if(i == 9)  printf(" Space: HARD DROP   ");
			else if(i == 10) printf(" z:    Rotate Left  ");
			else if(i == 11) printf(" c:    HOLD         ");
			else if(i == 12) printf(" p:    Pause        ");
			else if(i == 14) printf(" You can press:     ");
			else if(i == 15) printf(" s: Start           ");
			else if(i == 16) printf(" q: Quit            ");
			else{
				for(j = 0;j < col;j++)
					printf("  ");
			}
		}
		else if(runningState == 1){
			for(j = 0;j < col;j++){
				if(space[i][j] + movingBlock[i][j] != 0){
					printf("█");
				}
				else
					printf("  ");
			}
		}
		else if(runningState == 2){
			if(i == 2)       printf(" Pause              ");
			else if(i == 4)  printf(" Control Options:   ");
			else if(i == 5)  printf(" Left: Move Left    ");
			else if(i == 6)  printf(" Right: Move Right  ");
			else if(i == 7)  printf(" Up:   Rotate Right ");
			else if(i == 8)  printf(" Down: SOFT DROP    ");
			else if(i == 9)  printf(" Space: HARD DROP   ");
			else if(i == 10) printf(" z:    Rotate Left  ");
			else if(i == 11) printf(" c:    HOLD         ");
			else if(i == 12) printf(" p:    Pause        ");
			else if(i == 14) printf(" You can press:     ");
			else if(i == 15) printf(" p: Return          ");
			else if(i == 16) printf(" r: Restart         ");
			else if(i == 17) printf(" q: Quit            ");
			else{
				for(j = 0;j < col;j++)
					printf("  ");
			}
		}

		// The right list.
		if(i == 2)
			printf("┃ ┃   NEXT   ┃\n");
		else if(i == 3 || i == 6 || i == 9)
			printf("┃ ┃          ┃\n");
		else if(i == 4 || i == 7 || i == 10){
			if(runningState == 1){
				if(nextBlock[(i-4)/3+1] == 1)
					printf("┃ ┃          ┃\n");
				else if(nextBlock[(i-4)/3+1] == 2)
					printf("┃ ┃   █     ┃\n");
				else if(nextBlock[(i-4)/3+1] == 3)
					printf("┃ ┃       █ ┃\n");
				else if(nextBlock[(i-4)/3+1] == 4)
					printf("┃ ┃   ██   ┃\n");
				else if(nextBlock[(i-4)/3+1] == 5)
					printf("┃ ┃     ██ ┃\n");
				else if(nextBlock[(i-4)/3+1] == 6)
					printf("┃ ┃     █   ┃\n");
				else  // nextBlock == 7
					printf("┃ ┃   ██   ┃\n");
			}
			else
				printf("┃ ┃          ┃\n");
		}
		else if(i == 5 || i == 8 || i == 11){
			if(runningState == 1){
				if(nextBlock[(i-5)/3+1] == 1)
					printf("┃ ┃ ████ ┃\n");
				else if(nextBlock[(i-5)/3+1] == 2)
					printf("┃ ┃   ███ ┃\n");
				else if(nextBlock[(i-5)/3+1] == 3)
					printf("┃ ┃   ███ ┃\n");
				else if(nextBlock[(i-5)/3+1] == 4)
					printf("┃ ┃   ██   ┃\n");
				else if(nextBlock[(i-5)/3+1] == 5)
					printf("┃ ┃   ██   ┃\n");
				else if(nextBlock[(i-5)/3+1] == 6)
					printf("┃ ┃   ███ ┃\n");
				else  // nextBlock == 7
					printf("┃ ┃     ██ ┃\n");
			}
			else
				printf("┃ ┃          ┃\n");
		}
		else if(i == 12)
			printf("┃ ┗━━━━━━━━━━┛\n");
		else
			printf("┃\n");
	}


	// The last line.
	printf("              ┗");
	for(i = 0;i < col;i++)
		printf("━━");
	printf("┛\n");


	return;
}

void creatBlock()
{
	int base = col / 2;
	
	if(nextBlock[0] == 1){
		movingBlock[1][base-2] = movingBlock[1][base-1] = movingBlock[1][base+1] = 1;
		movingBlock[1][base] = 3;
	}
	else if(nextBlock[0] == 2){
		movingBlock[0][base-2] = movingBlock[1][base-2] = movingBlock[1][base] = 1;
		movingBlock[1][base-1] = 3;
	}
	else if(nextBlock[0] == 3){
		movingBlock[0][base] = movingBlock[1][base-2] = movingBlock[1][base] = 1;
		movingBlock[1][base-1] = 3;
	}
	else if(nextBlock[0] == 4){
		movingBlock[0][base-1] = movingBlock[0][base] = 1;
		movingBlock[1][base-1] = movingBlock[1][base] = 1;
	}
	else if(nextBlock[0] == 5){
		movingBlock[0][base] = movingBlock[1][base-2] = movingBlock[1][base-1] = 1;
		movingBlock[0][base-1] = 3;
	}
	else if(nextBlock[0] == 6){
		movingBlock[0][base-1] = movingBlock[1][base-2] = movingBlock[1][base] = 1;
		movingBlock[1][base-1] = 3;
	}
	else{  // nextBlock == 7
		movingBlock[0][base-2] = movingBlock[1][base-1] = movingBlock[1][base] = 1;
		movingBlock[0][base-1] = 3;
	}
	nowBlock = nextBlock[0];
	if(nextBlock[0] == nextBlock[1]){
		holdState = 0;
		nextBlock[0] = nextBlock[1] = nextBlock[2];
		nextBlock[2] = nextBlock[3];
		nextBlock[3] = 1+(int)((7.0)*rand()/(RAND_MAX+1.0));
	}
	else
		nextBlock[0] = nextBlock[1];
	return;
}

int winSize(int height, int width)
{
	HANDLE hOut = GetStdHandle(STD_OUTPUT_HANDLE);
	COORD bufferSize = {0};
	SMALL_RECT windowSize = {0};
	bufferSize.X = width;
	bufferSize.Y = height;
	windowSize.Right = width - 1;
	windowSize.Bottom = height - 1;
	SetConsoleWindowInfo(hOut, TRUE, &windowSize);
	SetConsoleScreenBufferSize(hOut, bufferSize);
	return 0;
}